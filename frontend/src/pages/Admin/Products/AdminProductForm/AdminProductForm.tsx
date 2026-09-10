import { useState, useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Upload, X, GripVertical } from "lucide-react";
import { useProducts } from "../../../../hooks/features/useProducts";
import { useNotification } from "../../../../context/useNotification";
import * as productsApi from "../../../../api/products";
import Button from "../../../../components/UI/Button/Button";
import Input from "../../../../components/UI/Input/Input";
import Loading from "../../../../components/UI/Loading/Loading";
import {
  CATEGORY_DISPLAY,
  type Category,
  type ProductImageResponse,
} from "../../../../types";
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  rectSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import styles from "./AdminProductForm.module.css";

interface SortableImageProps {
  img: ProductImageResponse;
  index: number;
  onRemove: (id: number) => void;
}

function SortableImage({ img, index, onRemove }: SortableImageProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: img.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div ref={setNodeRef} style={style} className={styles.imageItem}>
      <img src={img.url} alt={`Product image ${index + 1}`} />
      <button
        type="button"
        className={styles.dragHandle}
        aria-label={`Reorder image ${index + 1}`}
        {...attributes}
        {...listeners}
      >
        <GripVertical size={14} />
      </button>
      <button
        type="button"
        onClick={() => onRemove(img.id)}
        className={styles.removeBtn}
        aria-label={`Remove image ${index + 1}`}
      >
        <X size={14} />
      </button>
    </div>
  );
}

export default function AdminProductForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();
  const { product, productLoading, getProduct } = useProducts();
  const { showNotification } = useNotification();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");
  const [category, setCategory] = useState<Category>("ROLL");
  const [weight, setWeight] = useState("");
  const [pieces, setPieces] = useState("");
  const [images, setImages] = useState<File[]>([]);
  const [existingImages, setExistingImages] = useState<ProductImageResponse[]>(
    [],
  );
  const [isSubmitting, setIsSubmitting] = useState(false);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  useEffect(() => {
    if (isEdit && id) getProduct(Number(id));
  }, [isEdit, id]);

  useEffect(() => {
    if (isEdit && product) {
      setName(product.name);
      setDescription(product.description || "");
      setPrice(product.price.toString());
      setCategory(product.category as Category);
      setWeight(product.weight?.toString() || "");
      setPieces(product.pieces?.toString() || "");
      setExistingImages(product.images);
    }
  }, [isEdit, product]);

  const handleImageAdd = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files) setImages((prev) => [...prev, ...Array.from(files)]);
  };

  const handleRemoveNewImage = (index: number) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
  };

  const handleRemoveExistingImage = async (imageId: number) => {
    if (isEdit && id) {
      try {
        await productsApi.deleteProductImage(Number(id), imageId);
        setExistingImages((prev) => prev.filter((img) => img.id !== imageId));
        showNotification("Image removed", "success");
      } catch (err: unknown) {
        const message =
          (err as { response?: { data?: { message?: string } } })?.response
            ?.data?.message || "Failed to remove image";
        showNotification(message, "error");
      }
    }
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    setExistingImages((prev) => {
      const oldIndex = prev.findIndex((img) => img.id === active.id);
      const newIndex = prev.findIndex((img) => img.id === over.id);
      const updated = [...prev];
      const [moved] = updated.splice(oldIndex, 1);
      updated.splice(newIndex, 0, moved);
      return updated;
    });
  };

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      if (isEdit && id) {
        await productsApi.updateProduct(Number(id), {
          name: name || undefined,
          description: description || undefined,
          price: price ? Number(price) : undefined,
          category: category || undefined,
          weight: weight ? Number(weight) : undefined,
          pieces: pieces ? Number(pieces) : undefined,
        });

        const imageIds = existingImages.map((img) => img.id);
        if (imageIds.length > 0) {
          await productsApi.reorderProductImages(Number(id), imageIds);
        }

        if (images.length > 0) {
          for (const image of images) {
            await productsApi.addProductImage(Number(id), image);
          }
        }
        showNotification("Product updated", "success");
      } else {
        await productsApi.createProduct(
          {
            name,
            description: description || undefined,
            price: Number(price),
            category,
            weight: weight ? Number(weight) : undefined,
            pieces: pieces ? Number(pieces) : undefined,
          },
          images.length > 0 ? images : undefined,
        );
        showNotification("Product created", "success");
      }
      navigate("/admin/products");
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || "Failed to save product";
      showNotification(message, "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  const categories = Object.keys(CATEGORY_DISPLAY) as Category[];

  if (isEdit && productLoading) return <Loading text="Loading product..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <button
          onClick={() => navigate("/admin/products")}
          className={styles.backBtn}
        >
          <ArrowLeft size={20} />
        </button>
        <h1>{isEdit ? "Edit Product" : "New Product"}</h1>
      </div>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.layout}>
          <div className={styles.imagesSection}>
            <label className={styles.label}>Images</label>
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={handleDragEnd}
            >
              <SortableContext
                items={existingImages.map((img) => img.id)}
                strategy={rectSortingStrategy}
              >
                <div className={styles.imageGrid}>
                  {existingImages.map((img, index) => (
                    <SortableImage
                      key={img.id}
                      img={img}
                      index={index}
                      onRemove={handleRemoveExistingImage}
                    />
                  ))}
                  {images.map((file, index) => (
                    <div key={`new-${index}`} className={styles.imageItem}>
                      <img src={URL.createObjectURL(file)} alt={`New image ${index + 1}`} />
                      <button
                        type="button"
                        onClick={() => handleRemoveNewImage(index)}
                        className={styles.removeBtn}
                        aria-label={`Remove new image ${index + 1}`}
                      >
                        <X size={14} />
                      </button>
                    </div>
                  ))}
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className={styles.addImage}
                  >
                    <Upload size={20} />
                    <span>Add</span>
                  </button>
                </div>
              </SortableContext>
            </DndContext>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              onChange={handleImageAdd}
              hidden
            />
          </div>

          <div className={styles.fields}>
            <Input
              label="Name"
              value={name}
              onChange={setName}
              placeholder="Product name"
            />
            <div className={styles.fieldGroup}>
              <label className={styles.label}>Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                className={styles.textarea}
                placeholder="Product description"
                maxLength={250}
              />
            </div>
            <div className={styles.row}>
              <Input
                label="Weight (g)"
                value={weight}
                onChange={setWeight}
                placeholder="250"
                type="number"
              />
              <Input
                label="Pieces"
                value={pieces}
                onChange={setPieces}
                placeholder="8"
                type="number"
              />
            </div>
            <Input
              label="Price (₴)"
              value={price}
              onChange={setPrice}
              placeholder="250.00"
              type="number"
            />
            <div className={styles.fieldGroup}>
              <label className={styles.label}>Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as Category)}
                className={styles.select}
              >
                {categories.map((c) => (
                  <option key={c} value={c}>
                    {CATEGORY_DISPLAY[c]}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className={styles.actions}>
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate("/admin/products")}
          >
            Cancel
          </Button>
          <Button type="submit" loading={isSubmitting}>
            {isEdit ? "Update Product" : "Create Product"}
          </Button>
        </div>
      </form>
    </div>
  );
}
