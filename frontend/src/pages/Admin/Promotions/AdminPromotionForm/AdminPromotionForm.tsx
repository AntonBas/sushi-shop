import { useState, useEffect, useRef } from "react";
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
import { toDateTimeLocalValue } from "../../../../utils/dateTimeLocal";
import { formatPrice } from "../../../../utils/formatPrice";
import styles from "./AdminPromotionForm.module.css";

export default function AdminPromotionForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const { showNotification } = useNotification();
  const createApi = useApi<PromotionResponse>();
  const updateApi = useApi<PromotionResponse>();
  const { loading: getLoading, run: getPromotion } = useApi<PromotionResponse>();
  const { products, totalPages, loading, loadProducts } = useProducts();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [discountPercent, setDiscountPercent] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [loadedDates, setLoadedDates] = useState({ startDate: "", endDate: "" });
  const [active, setActive] = useState(true);
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([]);
  const [selectedProductNames, setSelectedProductNames] = useState<
    Record<number, string>
  >({});
  const [productSearch, setProductSearch] = useState("");
  const [debouncedProductSearch, setDebouncedProductSearch] = useState("");
  const productSearchDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [productPage, setProductPage] = useState(0);

  useEffect(() => {
    if (isEdit) {
      void getPromotion(() => promotionsApi.getPromotionById(Number(id)))
        .then((promo) => {
          if (!promo) return;
          setTitle(promo.title);
          setDescription(promo.description || "");
          setDiscountPercent(String(promo.discountPercent));
          const loadedStart = promo.startDate ? toDateTimeLocalValue(promo.startDate) : "";
          const loadedEnd = promo.endDate ? toDateTimeLocalValue(promo.endDate) : "";
          setStartDate(loadedStart);
          setEndDate(loadedEnd);
          setLoadedDates({ startDate: loadedStart, endDate: loadedEnd });
          setActive(promo.active);
          setSelectedProductIds(promo.products.map((p) => p.id));
          setSelectedProductNames(
            Object.fromEntries(promo.products.map((p) => [p.id, p.name])),
          );
        });
    }
  }, [id, isEdit, getPromotion]);

  useEffect(() => {
    void loadProducts(productPage, {
      search: debouncedProductSearch || undefined,
      available: true,
    });
  }, [productPage, debouncedProductSearch, loadProducts]);

  useEffect(() => () => {
    if (productSearchDebounceRef.current) clearTimeout(productSearchDebounceRef.current);
  }, []);

  const handleProductSearchChange = (value: string) => {
    setProductSearch(value);
    setProductPage(0);
    if (productSearchDebounceRef.current) clearTimeout(productSearchDebounceRef.current);
    productSearchDebounceRef.current = setTimeout(() => setDebouncedProductSearch(value), 300);
  };

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
    if (!startDate || !endDate) {
      showNotification("Start and end dates are required", "error");
      return;
    }
    const startIso = new Date(startDate).toISOString();
    const endIso = new Date(endDate).toISOString();
    const payload = {
      title,
      description: description || undefined,
      discountPercent: Number(discountPercent),
      startDate: startIso,
      endDate: endIso,
      productIds: selectedProductIds,
    };

    try {
      if (isEdit) {
        await updateApi.execute(() =>
          promotionsApi.updatePromotion(Number(id), {
            ...payload,
            startDate: startDate !== loadedDates.startDate ? startIso : undefined,
            endDate: endDate !== loadedDates.endDate ? endIso : undefined,
            active,
          }),
        );
        showNotification("Promotion updated", "success");
      } else {
        await createApi.execute(() => promotionsApi.createPromotion(payload));
        showNotification("Promotion created", "success");
      }
    } catch {
      return;
    }
    void navigate("/admin/promotions");
  };

  if (isEdit && getLoading) return <Loading text="Loading promotion..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <button
          type="button"
          onClick={() => void navigate("/admin/promotions")}
          className={styles.backBtn}
          aria-label="Back to promotions"
        >
          <ArrowLeft size={20} />
        </button>
        <h1>{isEdit ? "Edit Promotion" : "New Promotion"}</h1>
      </div>
      <form onSubmit={(e) => void handleSubmit(e)} className={styles.form}>
        <Input
          label="Title"
          value={title}
          onChange={setTitle}
          placeholder="Weekend Sale"
        />
        <div className={styles.fieldGroup}>
          <label htmlFor="promotion-description" className={styles.label}>Description</label>
          <textarea
            id="promotion-description"
            name="description"
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
            <span id="promotion-status-label" className={styles.label}>Status</span>
            <div className={styles.toggleRow} role="group" aria-labelledby="promotion-status-label">
              <button
                type="button"
                aria-pressed={active}
                onClick={() => setActive(true)}
                className={`${styles.statusBtn} ${active ? styles.statusBtnActive : ""}`}
              >
                Active
              </button>
              <button
                type="button"
                aria-pressed={!active}
                onClick={() => setActive(false)}
                className={`${styles.statusBtn} ${!active ? styles.statusBtnInactive : ""}`}
              >
                Inactive
              </button>
            </div>
          </div>
        )}
        <div className={styles.fieldGroup}>
          <span className={styles.label}>
            Products ({selectedProductIds.length} selected)
          </span>
          {selectedProductIds.length > 0 && (
            <div className={styles.selectedList}>
              {selectedProductIds.map((pid) => (
                <button
                  type="button"
                  key={pid}
                  className={styles.selectedTag}
                  onClick={() => removeSelected(pid)}
                  aria-label={`Remove ${selectedProductNames[pid] || `#${pid}`}`}
                >
                  {selectedProductNames[pid] || `#${pid}`}
                  <X size={12} aria-hidden="true" />
                </button>
              ))}
            </div>
          )}
          <div className={styles.searchBox}>
            <Search size={14} />
            <input
              type="text"
              placeholder="Search products..."
              aria-label="Search products"
              value={productSearch}
              onChange={(e) => handleProductSearchChange(e.target.value)}
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
                      ₴{formatPrice(product.price)}
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
            onClick={() => void navigate("/admin/promotions")}
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
