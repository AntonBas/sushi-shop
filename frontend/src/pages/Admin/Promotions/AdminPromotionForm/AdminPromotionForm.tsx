import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Search, X } from "lucide-react";
import { useApi } from "../../../../hooks/common/useApi";
import { useProducts } from "../../../../hooks/features/useProducts";
import { useNotification } from "../../../../context/useNotification";
import * as promotionsApi from "../../../../api/promotions";
import Button from "../../../../components/UI/Button/Button";
import Input from "../../../../components/UI/Input/Input";
import Loading from "../../../../components/UI/Loading/Loading";
import Pagination from "../../../../components/UI/Pagination/Pagination";
import type { PromotionResponse } from "../../../../types";
import styles from "./AdminPromotionForm.module.css";

export default function AdminPromotionForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const { showNotification } = useNotification();
  const createApi = useApi<PromotionResponse>();
  const updateApi = useApi<PromotionResponse>();
  const { loading: getLoading, execute: getPromotion } = useApi<PromotionResponse>();
  const { products, totalPages, loading, loadProducts } = useProducts();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [discountPercent, setDiscountPercent] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [active, setActive] = useState(true);
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([]);
  const [selectedProductNames, setSelectedProductNames] = useState<
    Record<number, string>
  >({});
  const [productSearch, setProductSearch] = useState("");
  const [productPage, setProductPage] = useState(0);

  useEffect(() => {
    if (isEdit) {
      getPromotion(() => promotionsApi.getPromotionById(Number(id)))
        .then((promo) => {
          if (!promo) return;
          setTitle(promo.title);
          setDescription(promo.description || "");
          setDiscountPercent(String(promo.discountPercent));
          setStartDate(promo.startDate?.slice(0, 16) || "");
          setEndDate(promo.endDate?.slice(0, 16) || "");
          setActive(promo.active);
          setSelectedProductIds(promo.products.map((p) => p.id));
          setSelectedProductNames(
            Object.fromEntries(promo.products.map((p) => [p.id, p.name])),
          );
        });
    }
  }, [id, isEdit, getPromotion]);

  useEffect(() => {
    loadProducts(productPage, {
      search: productSearch || undefined,
      available: true,
    });
  }, [productPage, productSearch, loadProducts]);

  const toggleProduct = (pid: number, name: string) => {
    setSelectedProductIds((prev) => {
      if (prev.includes(pid)) {
        setSelectedProductNames((names) => {
          const updated = { ...names };
          delete updated[pid];
          return updated;
        });
        return prev.filter((p) => p !== pid);
      } else {
        setSelectedProductNames((names) => ({ ...names, [pid]: name }));
        return [...prev, pid];
      }
    });
  };

  const removeSelected = (pid: number) => {
    setSelectedProductIds((prev) => prev.filter((p) => p !== pid));
    setSelectedProductNames((names) => {
      const updated = { ...names };
      delete updated[pid];
      return updated;
    });
  };

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    const payload = {
      title,
      description: description || undefined,
      discountPercent: Number(discountPercent),
      startDate: new Date(startDate).toISOString(),
      endDate: new Date(endDate).toISOString(),
      productIds: selectedProductIds,
      ...(isEdit && { active }),
    };

    try {
      if (isEdit) {
        await updateApi.execute(() =>
          promotionsApi.updatePromotion(Number(id), payload),
        );
        showNotification("Promotion updated", "success");
      } else {
        await createApi.execute(() => promotionsApi.createPromotion(payload));
        showNotification("Promotion created", "success");
      }
      navigate("/admin/promotions");
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || `Failed to ${isEdit ? "update" : "create"} promotion`;
      showNotification(message, "error");
    }
  };

  if (isEdit && getLoading) return <Loading text="Loading promotion..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <button
          type="button"
          onClick={() => navigate("/admin/promotions")}
          className={styles.backBtn}
          aria-label="Back to promotions"
        >
          <ArrowLeft size={20} />
        </button>
        <h1>{isEdit ? "Edit Promotion" : "New Promotion"}</h1>
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
        {isEdit && (
          <div className={styles.fieldGroup}>
            <label className={styles.label}>Status</label>
            <div className={styles.toggleRow}>
              <button
                type="button"
                onClick={() => setActive(true)}
                className={`${styles.statusBtn} ${active ? styles.statusBtnActive : ""}`}
              >
                Active
              </button>
              <button
                type="button"
                onClick={() => setActive(false)}
                className={`${styles.statusBtn} ${!active ? styles.statusBtnInactive : ""}`}
              >
                Inactive
              </button>
            </div>
          </div>
        )}
        <div className={styles.fieldGroup}>
          <label className={styles.label}>
            Products ({selectedProductIds.length} selected)
          </label>
          {selectedProductIds.length > 0 && (
            <div className={styles.selectedList}>
              {selectedProductIds.map((pid) => (
                <span
                  key={pid}
                  className={styles.selectedTag}
                  onClick={() => removeSelected(pid)}
                >
                  {selectedProductNames[pid] || `#${pid}`}
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
          <Button
            type="submit"
            loading={isEdit ? updateApi.loading : createApi.loading}
          >
            {isEdit ? "Update Promotion" : "Create Promotion"}
          </Button>
        </div>
      </form>
    </div>
  );
}
