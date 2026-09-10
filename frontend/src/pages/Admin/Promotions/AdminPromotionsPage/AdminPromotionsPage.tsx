import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Pencil, Trash2, Search } from "lucide-react";
import { useApi } from "../../../../hooks/common/useApi";
import { useNotification } from "../../../../context/useNotification";
import * as promotionsApi from "../../../../api/promotions";
import Button from "../../../../components/UI/Button/Button";
import Loading from "../../../../components/UI/Loading/Loading";
import Pagination from "../../../../components/UI/Pagination/Pagination";
import Modal from "../../../../components/UI/Modal/Modal";
import type { PromotionResponse } from "../../../../types";
import type { Page } from "../../../../types/common";
import styles from "./AdminPromotionsPage.module.css";

export default function AdminPromotionsPage() {
  const navigate = useNavigate();
  const { showNotification } = useNotification();
  const { data, loading, execute } = useApi<Page<PromotionResponse>>();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteTitle, setDeleteTitle] = useState("");

  const loadPromotions = useCallback(
    (p: number, s?: string) => {
      return execute(() => promotionsApi.getPromotions(p, 12, s));
    },
    [execute],
  );

  useEffect(() => {
    loadPromotions(page, search || undefined);
  }, [page, search, loadPromotions]);

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await promotionsApi.deletePromotion(deleteId);
      setDeleteId(null);
      showNotification("Promotion deleted", "success");
      loadPromotions(page, search || undefined);
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || "Failed to delete promotion";
      showNotification(message, "error");
    }
  };

  if (loading && !data) return <Loading text="Loading promotions..." />;

  const promotions = data?.content || [];

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Promotions</h1>
        <Button onClick={() => navigate("/admin/promotions/new")}>
          Add Promotion
        </Button>
      </div>
      <div className={styles.filters}>
        <div className={styles.searchBox}>
          <Search size={16} />
          <input
            type="text"
            placeholder="Search promotions..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
      </div>
      {promotions.length === 0 ? (
        <div className={styles.empty}>
          <h3>No promotions found</h3>
          <p>Create your first promotion</p>
          <Button onClick={() => navigate("/admin/promotions/new")}>
            Add Promotion
          </Button>
        </div>
      ) : (
        <>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Discount</th>
                  <th>Period</th>
                  <th>Products</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {promotions.map((promo) => (
                  <tr key={promo.id}>
                    <td data-label="Title" className={styles.name}>
                      {promo.title}
                    </td>
                    <td data-label="Discount">{promo.discountPercent}%</td>
                    <td data-label="Period">
                      {promo.startDate
                        ? new Date(promo.startDate).toLocaleDateString()
                        : "—"}{" "}
                      –{" "}
                      {promo.endDate
                        ? new Date(promo.endDate).toLocaleDateString()
                        : "—"}
                    </td>
                    <td data-label="Products">{promo.products.length}</td>
                    <td data-label="Status">
                      <span
                        className={`${styles.statusBadge} ${promo.isCurrentlyActive ? styles.active : styles.inactive}`}
                      >
                        {promo.isCurrentlyActive ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td data-label="Actions">
                      <div className={styles.actions}>
                        <button
                          type="button"
                          onClick={() =>
                            navigate(`/admin/promotions/${promo.id}/edit`)
                          }
                          className={styles.editBtn}
                          aria-label="Edit promotion"
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setDeleteId(promo.id);
                            setDeleteTitle(promo.title);
                          }}
                          className={styles.deleteBtn}
                          aria-label="Delete promotion"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination
            currentPage={page}
            totalPages={data?.page.totalPages || 0}
            onPageChange={setPage}
          />
        </>
      )}
      <Modal
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        title="Delete Promotion"
      >
        <p>Are you sure you want to delete "{deleteTitle}"?</p>
        <div
          style={{
            display: "flex",
            gap: 8,
            justifyContent: "flex-end",
            marginTop: 16,
          }}
        >
          <Button onClick={() => setDeleteId(null)} variant="secondary">
            Cancel
          </Button>
          <Button onClick={handleDelete} variant="danger">
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
