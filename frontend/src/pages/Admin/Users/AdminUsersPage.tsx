import { useEffect, useState } from "react";
import { Search } from "lucide-react";
import { useAdminUsers } from "../../../hooks/features/useAdminUsers";
import { useAuth } from "../../../context/useAuth";
import { useNotification } from "../../../context/useNotification";
import * as adminUsersApi from "../../../api/adminUsers";
import { getErrorMessage } from "../../../api/errorMessage";
import { formatPrice } from "../../../utils/formatPrice";
import { pickAllowed, useDebouncedParamInput, useListSearchParams } from "../../../hooks/common/useListSearchParams";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import Button from "../../../components/UI/Button/Button";
import Modal from "../../../components/UI/Modal/Modal";
import { USER_ROLE_LABELS, USER_ROLES } from "../../../types/enums";
import type { AdminUserResponse, UserRole } from "../../../types";
import styles from "./AdminUsersPage.module.css";

const SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Newest" },
  { value: "createdAt,asc", label: "Oldest" },
  { value: "name,asc", label: "Name A–Z" },
  { value: "totalSpent,desc", label: "Top spenders" },
  { value: "ordersCount,desc", label: "Most orders" },
] as const;

const SORT_VALUES = SORT_OPTIONS.map((option) => option.value);
const VERIFIED_VALUES = ["true", "false"] as const;

interface PendingRoleChange {
  user: AdminUserResponse;
  role: UserRole;
}

export default function AdminUsersPage() {
  const { users, totalPages, loading, loadUsers } = useAdminUsers();
  const { user: currentUser } = useAuth();
  const { showNotification } = useNotification();
  const { page, getParam, updateParams, setPage, keepPageInRange } = useListSearchParams();
  const search = getParam("search");
  const role = pickAllowed(getParam("role"), USER_ROLES);
  const verified = pickAllowed(getParam("verified"), VERIFIED_VALUES);
  const sort = pickAllowed(getParam("sort"), SORT_VALUES) || "createdAt,desc";
  const [searchInput, handleSearchChange] = useDebouncedParamInput(search, (value) =>
    updateParams({ search: value }, { replace: true }),
  );
  const [pendingChange, setPendingChange] = useState<PendingRoleChange | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    void loadUsers(page, sort, {
      search: search || undefined,
      role: role || undefined,
      emailVerified: verified ? verified === "true" : undefined,
    });
  }, [page, sort, search, role, verified, loadUsers]);

  useEffect(() => {
    keepPageInRange(totalPages);
  }, [totalPages, keepPageInRange]);

  const handleConfirmRoleChange = async () => {
    if (!pendingChange) return;
    setSaving(true);
    try {
      await adminUsersApi.changeUserRole(pendingChange.user.id, pendingChange.role);
      showNotification(
        `${pendingChange.user.email} is now ${USER_ROLE_LABELS[pendingChange.role]}`,
        "success",
      );
      setPendingChange(null);
      void loadUsers(page, sort, {
        search: search || undefined,
        role: role || undefined,
        emailVerified: verified ? verified === "true" : undefined,
      });
    } catch (err: unknown) {
      showNotification(getErrorMessage(err, "Failed to change role"), "error");
    } finally {
      setSaving(false);
    }
  };

  if (loading && users.length === 0) return <Loading text="Loading users..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Users</h1>
      </div>

      <div className={styles.filters}>
        <div className={styles.searchBox}>
          <Search size={16} />
          <input
            type="text"
            placeholder="Search by name or email"
            value={searchInput}
            onChange={(e) => handleSearchChange(e.target.value)}
            aria-label="Search users"
          />
        </div>
        <select
          value={role}
          onChange={(e) => updateParams({ role: e.target.value })}
          className={styles.filterSelect}
          aria-label="Filter by role"
        >
          <option value="">All Roles</option>
          {USER_ROLES.map((value) => (
            <option key={value} value={value}>
              {USER_ROLE_LABELS[value]}
            </option>
          ))}
        </select>
        <select
          value={verified}
          onChange={(e) => updateParams({ verified: e.target.value })}
          className={styles.filterSelect}
          aria-label="Filter by email verification"
        >
          <option value="">Any Email Status</option>
          <option value="true">Verified</option>
          <option value="false">Not Verified</option>
        </select>
        <select
          value={sort}
          onChange={(e) => updateParams({ sort: e.target.value === "createdAt,desc" ? "" : e.target.value })}
          className={styles.filterSelect}
          aria-label="Sort users"
        >
          {SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {users.length === 0 ? (
        <div className={styles.empty}>
          <h3>No users found</h3>
        </div>
      ) : (
        <>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>Email</th>
                  <th>Sign-in</th>
                  <th>Orders</th>
                  <th>Spent</th>
                  <th>Registered</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const isSelf = user.id === currentUser?.id;
                  return (
                    <tr key={user.id}>
                      <td data-label="User">
                        <div className={styles.userCell}>
                          <span className={styles.userName}>{user.name}</span>
                          <span className={styles.userEmail}>{user.email}</span>
                          {user.phone && <span className={styles.userEmail}>{user.phone}</span>}
                        </div>
                      </td>
                      <td data-label="Role">
                        <select
                          value={user.userRole}
                          disabled={isSelf}
                          title={isSelf ? "You cannot change your own role" : undefined}
                          onChange={(e) => setPendingChange({ user, role: e.target.value as UserRole })}
                          className={styles.roleSelect}
                          aria-label={`Role of ${user.email}`}
                        >
                          {USER_ROLES.map((value) => (
                            <option key={value} value={value}>
                              {USER_ROLE_LABELS[value]}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td data-label="Email">
                        <span className={user.emailVerified ? styles.badgeSuccess : styles.badgeMuted}>
                          {user.emailVerified ? "Verified" : "Not verified"}
                        </span>
                      </td>
                      <td data-label="Sign-in">{user.hasPassword ? "Password" : "Google"}</td>
                      <td data-label="Orders">{user.ordersCount}</td>
                      <td data-label="Spent">{formatPrice(user.totalSpent)}₴</td>
                      <td data-label="Registered">{new Date(user.createdAt).toLocaleDateString()}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <Modal
        isOpen={!!pendingChange}
        onClose={() => setPendingChange(null)}
        title="Change Role"
      >
        {pendingChange && (
          <p>
            Change role of {pendingChange.user.email} from{" "}
            {USER_ROLE_LABELS[pendingChange.user.userRole]} to {USER_ROLE_LABELS[pendingChange.role]}?
            They will be signed out of all sessions.
          </p>
        )}
        <div className={styles.modalActions}>
          <Button onClick={() => setPendingChange(null)} variant="secondary">
            Cancel
          </Button>
          <Button onClick={() => void handleConfirmRoleChange()} loading={saving}>
            Change Role
          </Button>
        </div>
      </Modal>
    </div>
  );
}
