import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Search, X } from "lucide-react";
import { useApi } from "../../../../hooks/common/useApi";
import { useProducts } from "../../../../hooks/features/useProducts";
import { useNotification } from "../../../../context/NotificationContext";
import * as promotionsApi from "../../../../api/promotions";
import Button from "../../../../components/UI/Button/Button";
import Input from "../../../../components/UI/Input/Input";
import Loading from "../../../../components/UI/Loading/Loading";
import Pagination from "../../../../components/UI/Pagination/Pagination";
import type { PromotionResponse } from "../../../../types";
import styles from "./AdminPromotionForm.module.css";

export default function AdminPromotionForm() {
  const navigate = useNavigate();
  const { showNotification } = useNotification();
  const createApi = useApi<PromotionResponse>();
  const { products, totalPages, loading, loadProducts } = useProducts();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [discountPercent, setDiscountPercent] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([]);
  const [selectedProductNames, setSelectedProductNames] = useState<
    Record<number, string>
  >({});
  const [productSearch, setProductSearch] = useState("");
  const [productPage, setProductPage] = useState(0);

  useEffect(() => {
    loadProducts(productPage, {
      search: productSearch || undefined,
      available: true,
    });
  }, [productPage, productSearch]);

  const toggleProduct = (id: number, name: string) => {
    setSelectedProductIds((prev) => {
      if (prev.includes(id)) {
        setSelectedProductNames((names) => {
          const updated = { ...names };
          delete updated[id];
          return updated;
        });
        return prev.filter((p) => p !== id);
      } else {
        setSelectedProductNames((names) => ({ ...names, [id]: name }));
        return [...prev, id];
      }
    });
  };

  const removeSelected = (id: number) => {
    setSelectedProductIds((prev) => prev.filter((p) => p !== id));
    setSelectedProductNames((names) => {
      const updated = { ...names };
      delete updated[id];
      return updated;
    });
  };

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await createApi.execute(() =>
        promotionsApi.createPromotion({
          title,
          description: description || undefined,
          discountPercent: Number(discountPercent),
          startDate: new Date(startDate).toISOString(),
          endDate: new Date(endDate).toISOString(),
          productIds: selectedProductIds,
        }),
      );
      showNotification("Promotion created", "success");
      navigate("/admin/promotions");
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || "Failed to create promotion";
      showNotification(message, "error");
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <button
          onClick={() => navigate("/admin/promotions")}
          className={styles.backBtn}
        >
          <ArrowLeft size={20} />
        </button>
        <h1>New Promotion</h1>
      </div>
      <form onSubmit={handleSubmit} className={styles.form}>
        <Input
          label="Title"
          value={title}
          onChange={setTitle}
          placeholder="Weekend Sale"
        />
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            className={styles.textarea}
            placeholder="Promotion description"
            maxLength={250}
          />
        </div>
        <Input
          label="Discount (%)"
          value={discountPercent}
          onChange={setDiscountPercent}
          placeholder="20"
          type="number"
        />
        <div className={styles.row}>
          <Input
            label="Start Date"
            value={startDate}
            onChange={setStartDate}
            type="datetime-local"
          />
          <Input
            label="End Date"
            value={endDate}
            onChange={setEndDate}
            type="datetime-local"
          />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>
            Products ({selectedProductIds.length} selected)
          </label>
          {selectedProductIds.length > 0 && (
            <div className={styles.selectedList}>
              {selectedProductIds.map((id) => (
                <span
                  key={id}
                  className={styles.selectedTag}
                  onClick={() => removeSelected(id)}
                >
                  {selectedProductNames[id] || `#${id}`}
                  <X size={12} />
                </span>
              ))}
            </div>
          )}
          <div className={styles.searchBox}>
            <Search size={14} />
            <input
              type="text"
              placeholder="Search products..."
              value={productSearch}
              onChange={(e) => {
                setProductSearch(e.target.value);
                setProductPage(0);
              }}
            />
          </div>
          {loading ? (
            <Loading text="Loading products..." />
          ) : (
            <>
              <div className={styles.productList}>
                {products.map((product) => (
                  <button
                    key={product.id}
                    type="button"
                    onClick={() => toggleProduct(product.id, product.name)}
                    className={`${styles.productItem} ${selectedProductIds.includes(product.id) ? styles.selected : ""}`}
                  >
                    <span>{product.name}</span>
                    <span className={styles.productPrice}>
                      ₴{product.price}
                    </span>
                  </button>
                ))}
              </div>
              {totalPages > 1 && (
                <Pagination
                  currentPage={productPage}
                  totalPages={totalPages}
                  onPageChange={setProductPage}
                />
              )}
            </>
          )}
        </div>
        <div className={styles.actions}>
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate("/admin/promotions")}
          >
            Cancel
          </Button>
          <Button type="submit" loading={createApi.loading}>
            Create Promotion
          </Button>
        </div>
      </form>
    </div>
  );
}
