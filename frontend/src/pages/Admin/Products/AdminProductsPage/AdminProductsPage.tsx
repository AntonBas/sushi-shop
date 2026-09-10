import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Pencil, Trash2, ToggleLeft, ToggleRight, Search } from "lucide-react";
import { useProducts } from "../../../../hooks/features/useProducts";
import { useNotification } from "../../../../context/useNotification";
import * as productsApi from "../../../../api/products";
import Button from "../../../../components/UI/Button/Button";
import Loading from "../../../../components/UI/Loading/Loading";
import Pagination from "../../../../components/UI/Pagination/Pagination";
import Modal from "../../../../components/UI/Modal/Modal";
import { CATEGORY_DISPLAY } from "../../../../types/enums";
import type { Category } from "../../../../types";
import styles from "./AdminProductsPage.module.css";

export default function AdminProductsPage() {
  const { products, totalPages, loading, loadProducts } = useProducts();
  const { showNotification } = useNotification();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<Category | "">("");
  const [availableFilter, setAvailableFilter] = useState<boolean | "">("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteName, setDeleteName] = useState("");

  const categories = Object.keys(CATEGORY_DISPLAY) as Category[];

  useEffect(() => {
    loadProducts(page, {
      search: search || undefined,
      category: categoryFilter || undefined,
      available: availableFilter === "" ? undefined : availableFilter,
    });
  }, [page, search, categoryFilter, availableFilter]);

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await productsApi.deleteProduct(deleteId);
      setDeleteId(null);
      showNotification("Product deleted", "success");
      loadProducts(page, {
        search: search || undefined,
        category: categoryFilter || undefined,
        available: availableFilter === "" ? undefined : availableFilter,
      });
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || "Failed to delete product";
      showNotification(message, "error");
    }
  };

  const handleToggle = async (id: number) => {
    try {
      await productsApi.toggleProduct(id);
      showNotification("Product status updated", "success");
      loadProducts(page, {
        search: search || undefined,
        category: categoryFilter || undefined,
        available: availableFilter === "" ? undefined : availableFilter,
      });
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || "Failed to update product";
      showNotification(message, "error");
    }
  };

  if (loading) return <Loading text="Loading products..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Products</h1>
        <Button onClick={() => navigate("/admin/products/new")}>
          Add Product
        </Button>
      </div>

      <div className={styles.filters}>
        <div className={styles.searchBox}>
          <Search size={16} />
          <input
            type="text"
            placeholder="Search products..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <select
          value={categoryFilter}
          onChange={(e) => {
            setCategoryFilter(e.target.value as Category | "");
            setPage(0);
          }}
          className={styles.filterSelect}
        >
          <option value="">All Categories</option>
          {categories.map((c) => (
            <option key={c} value={c}>
              {CATEGORY_DISPLAY[c]}
            </option>
          ))}
        </select>
        <select
          value={availableFilter === "" ? "" : availableFilter.toString()}
          onChange={(e) => {
            const val = e.target.value;
            setAvailableFilter(val === "" ? "" : val === "true");
            setPage(0);
          }}
          className={styles.filterSelect}
        >
          <option value="">All Status</option>
          <option value="true">Available</option>
          <option value="false">Unavailable</option>
        </select>
      </div>

      {products.length === 0 ? (
        <div className={styles.empty}>
          <h3>No products found</h3>
          <p>Get started by creating your first product</p>
          <Button onClick={() => navigate("/admin/products/new")}>
            Add Product
          </Button>
        </div>
      ) : (
        <>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Image</th>
                <th>Name</th>
                <th>Category</th>
                <th>Weight</th>
                <th>Pieces</th>
                <th>Price</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>
                    {product.mainImage ? (
                      <img
                        src={product.mainImage}
                        alt={product.name}
                        className={styles.thumb}
                        loading="lazy"
                      />
                    ) : (
                      <div className={styles.noImage}>—</div>
                    )}
                  </td>
                  <td className={styles.name}>{product.name}</td>
                  <td>
                    <span className={styles.badge}>
                      {CATEGORY_DISPLAY[product.category]}
                    </span>
                  </td>
                  <td>
                    {product.weight
                      ? `${product.weight}${product.category === "DRINK" ? "ml" : "g"}`
                      : "—"}
                  </td>
                  <td>{product.pieces ? `${product.pieces} pcs` : "—"}</td>
                  <td>{product.price}₴</td>
                  <td>
                    <button
                      onClick={() => handleToggle(product.id)}
                      className={styles.toggleBtn}
                    >
                      {product.available ? (
                        <ToggleRight size={20} className={styles.on} />
                      ) : (
                        <ToggleLeft size={20} className={styles.off} />
                      )}
                    </button>
                  </td>
                  <td>
                    <div className={styles.actions}>
                      <button
                        onClick={() =>
                          navigate(`/admin/products/${product.id}/edit`)
                        }
                        className={styles.editBtn}
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        onClick={() => {
                          setDeleteId(product.id);
                          setDeleteName(product.name);
                        }}
                        className={styles.deleteBtn}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </>
      )}

      <Modal
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        title="Delete Product"
      >
        <p>Are you sure you want to delete "{deleteName}"?</p>
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
