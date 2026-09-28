import { useState, useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Upload, X, GripVertical } from "lucide-react";
import { useProducts } from "../../../../hooks/features/useProducts";
import { useNotification } from "../../../../context/useNotification";
import * as productsApi from "../../../../api/products";
import { getErrorMessage } from "../../../../api/errorMessage";
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
import { useReturnTo } from "../../../../hooks/common/useReturnTo";
import styles from "./AdminProductForm.module.css";

const MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
const MAX_NEW_IMAGES = 5;

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
      <img src={img.url} alt={`Uploaded file ${index + 1}`} />
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

interface NewImage {
  file: File;
  previewUrl: string;
}

export default function AdminProductForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();
  const backTo = useReturnTo("/admin/products");
  const { product, productLoading, getProduct } = useProducts();
  const { showNotification } = useNotification();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");
  const [category, setCategory] = useState<Category>("ROLL");
  const [weight, setWeight] = useState("");
  const [pieces, setPieces] = useState("");
  const [images, setImages] = useState<NewImage[]>([]);
  const [removedImageIds, setRemovedImageIds] = useState<number[]>([]);
  const previewUrlsRef = useRef<string[]>([]);
  const [existingImages, setExistingImages] = useState<ProductImageResponse[]>(
    [],
  );
  const [isSubmitting, setIsSubmitting] = useState(false);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  useEffect(() => {
    if (isEdit && id) void getProduct(Number(id));
  }, [isEdit, id, getProduct]);

  const [loadedProduct, setLoadedProduct] = useState<typeof product>(null);

  if (isEdit && product && product !== loadedProduct) {
    setLoadedProduct(product);
    setName(product.name);
    setDescription(product.description || "");
    setPrice(product.price.toString());
    setCategory(product.category as Category);
    setWeight(product.weight?.toString() || "");
    setPieces(product.pieces?.toString() || "");
    setExistingImages(product.images);
  }

  useEffect(() => () => previewUrlsRef.current.forEach((url) => URL.revokeObjectURL(url)), []);

  const handleImageAdd = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    const selected = Array.from(files);
    e.target.value = "";
    const withinSize = selected.filter((file) => file.size <= MAX_IMAGE_SIZE_BYTES);
    if (withinSize.length < selected.length) {
      showNotification("Images larger than 5 MB were skipped", "warning");
    }
    const slotsLeft = isEdit ? withinSize.length : MAX_NEW_IMAGES - images.length;
    if (withinSize.length > slotsLeft) {
      showNotification(`You can add up to ${MAX_NEW_IMAGES} images when creating a product`, "warning");
    }
    const added = withinSize
      .slice(0, Math.max(0, slotsLeft))
      .map((file) => ({ file, previewUrl: URL.createObjectURL(file) }));
    previewUrlsRef.current.push(...added.map((image) => image.previewUrl));
    setImages((prev) => [...prev, ...added]);
  };

  const handleRemoveNewImage = (index: number) => {
    setImages((prev) => {
      URL.revokeObjectURL(prev[index].previewUrl);
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleRemoveExistingImage = (imageId: number) => {
    setExistingImages((prev) => prev.filter((img) => img.id !== imageId));
    setRemovedImageIds((prev) => [...prev, imageId]);
  };

  const handleDragEnd = (event: DragEndEvent) => {
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
          description: description || null,
          price: price ? Number(price) : undefined,
          category: category || undefined,
          weight: weight ? Number(weight) : undefined,
          pieces: pieces ? Number(pieces) : null,
        });

        for (const imageId of removedImageIds) {
          await productsApi.deleteProductImage(Number(id), imageId);
        }

        const imageIds = existingImages.map((img) => img.id);
        if (imageIds.length > 0) {
          await productsApi.reorderProductImages(Number(id), imageIds);
        }

        if (images.length > 0) {
          for (const image of images) {
            await productsApi.addProductImage(Number(id), image.file);
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
          images.length > 0 ? images.map((image) => image.file) : undefined,
        );
        showNotification("Product created", "success");
      }
      void navigate(backTo);
    } catch (err: unknown) {
      showNotification(getErrorMessage(err, "Failed to save product"), "error");
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
          type="button"
          onClick={() => void navigate(backTo)}
          className={styles.backBtn}
          aria-label="Back to products"
        >
          <ArrowLeft size={20} />
        </button>
        <h1>{isEdit ? "Edit Product" : "New Product"}</h1>
      </div>

      <form onSubmit={(e) => void handleSubmit(e)} className={styles.form}>
        <div className={styles.layout}>
          <div className={styles.imagesSection}>
            <span className={styles.label}>Images</span>
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
                  {images.map((image, index) => (
                    <div key={image.previewUrl} className={styles.imageItem}>
                      <img src={image.previewUrl} alt={`New upload ${index + 1}`} />
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
              accept="image/jpeg,image/png,image/webp"
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
              <label htmlFor="product-description" className={styles.label}>Description</label>
              <textarea
                id="product-description"
                name="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                className={styles.textarea}
                placeholder="Product description"
                maxLength={500}
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
              <label htmlFor="product-category" className={styles.label}>Category</label>
              <select
                id="product-category"
                name="category"
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
            onClick={() => void navigate(backTo)}
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
