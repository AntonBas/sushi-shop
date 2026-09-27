import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Pencil, Trash2, ToggleLeft, ToggleRight, Search } from "lucide-react";
import { useProducts } from "../../../../hooks/features/useProducts";
import { useNotification } from "../../../../context/useNotification";
import * as productsApi from "../../../../api/products";
import { getErrorMessage } from "../../../../api/errorMessage";
import { formatPrice } from "../../../../utils/formatPrice";
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
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<Category | "">("");
  const [availableFilter, setAvailableFilter] = useState<boolean | "">("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteName, setDeleteName] = useState("");
  const searchDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const categories = Object.keys(CATEGORY_DISPLAY) as Category[];

  useEffect(() => {
    void loadProducts(page, {
      search: debouncedSearch || undefined,
      category: categoryFilter || undefined,
      available: availableFilter === "" ? undefined : availableFilter,
    });
  }, [page, debouncedSearch, categoryFilter, availableFilter, loadProducts]);

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(0);
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    searchDebounceRef.current = setTimeout(() => setDebouncedSearch(value), 300);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await productsApi.deleteProduct(deleteId);
      setDeleteId(null);
      showNotification("Product deleted", "success");
      void loadProducts(page, {
        search: debouncedSearch || undefined,
        category: categoryFilter || undefined,
        available: availableFilter === "" ? undefined : availableFilter,
      });
    } catch (err: unknown) {
      showNotification(getErrorMessage(err, "Failed to delete product"), "error");
    }
  };

  const handleToggle = async (id: number) => {
    try {
      await productsApi.toggleProduct(id);
      showNotification("Product status updated", "success");
      void loadProducts(page, {
        search: debouncedSearch || undefined,
        category: categoryFilter || undefined,
        available: availableFilter === "" ? undefined : availableFilter,
      });
    } catch (err: unknown) {
      showNotification(getErrorMessage(err, "Failed to update product"), "error");
    }
  };

  if (loading && products.length === 0) return <Loading text="Loading products..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Products</h1>
        <Button onClick={() => void navigate("/admin/products/new")}>
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
            onChange={(e) => handleSearchChange(e.target.value)}
          />
        </div>
        <select
          value={categoryFilter}
          onChange={(e) => {
            setCategoryFilter(e.target.value as Category | "");
            setPage(0);
          }}
          className={styles.filterSelect}
          aria-label="Filter by category"
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
          aria-label="Filter by availability"
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
          <Button onClick={() => void navigate("/admin/products/new")}>
            Add Product
          </Button>
        </div>
      ) : (
        <>
          <div className={styles.tableWrapper}>
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
                    <td data-label="Image">
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
                    <td data-label="Name" className={styles.name}>{product.name}</td>
                    <td data-label="Category">
                      <span className={styles.badge}>
                        {CATEGORY_DISPLAY[product.category]}
                      </span>
                    </td>
                    <td data-label="Weight">
                      {product.weight
                        ? `${product.weight}${product.category === "DRINK" ? "ml" : "g"}`
                        : "—"}
                    </td>
                    <td data-label="Pieces">{product.pieces ? `${product.pieces} pcs` : "—"}</td>
                    <td data-label="Price">{formatPrice(product.price)}₴</td>
                    <td data-label="Status">
                      <button
                        type="button"
                        onClick={() => void handleToggle(product.id)}
                        className={styles.toggleBtn}
                        aria-label={`${product.available ? "Mark unavailable" : "Mark available"}: ${product.name}`}
                      >
                        {product.available ? (
                          <ToggleRight size={20} className={styles.on} />
                        ) : (
                          <ToggleLeft size={20} className={styles.off} />
                        )}
                      </button>
                    </td>
                    <td data-label="Actions">
                      <div className={styles.actions}>
                        <button
                          type="button"
                          onClick={() =>
                            void navigate(`/admin/products/${product.id}/edit`)
                          }
                          className={styles.editBtn}
                          aria-label={`Edit ${product.name}`}
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setDeleteId(product.id);
                            setDeleteName(product.name);
                          }}
                          className={styles.deleteBtn}
                          aria-label={`Delete ${product.name}`}
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
          <Button onClick={() => void handleDelete()} variant="danger">
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
