import { useState, useEffect, useRef } from "react";
import { useAuditLogs } from "../../../hooks/features/useAudit";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import type { AuditAction } from "../../../types";
import styles from "./AdminAuditPage.module.css";

interface TextFilters {
  entityName: string;
  entityId: string;
  performedBy: string;
}

const EMPTY_TEXT_FILTERS: TextFilters = { entityName: "", entityId: "", performedBy: "" };

export default function AdminAuditPage() {
  const { logs, totalPages, loading, loadLogs } = useAuditLogs();
  const [page, setPage] = useState(0);
  const [action, setAction] = useState<AuditAction | "">("");
  const [textFilters, setTextFilters] = useState<TextFilters>(EMPTY_TEXT_FILTERS);
  const [debouncedTextFilters, setDebouncedTextFilters] = useState<TextFilters>(EMPTY_TEXT_FILTERS);
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    loadLogs(page, {
      action: action || undefined,
      entityName: debouncedTextFilters.entityName || undefined,
      entityId: debouncedTextFilters.entityId ? Number(debouncedTextFilters.entityId) : undefined,
      performedBy: debouncedTextFilters.performedBy || undefined,
      start: start ? new Date(start).toISOString() : undefined,
      end: end ? new Date(end).toISOString() : undefined,
    });
  }, [page, action, debouncedTextFilters, start, end, loadLogs]);

  const handleTextFilterChange = (key: keyof TextFilters, value: string) => {
    const next = { ...textFilters, [key]: value };
    setTextFilters(next);
    setPage(0);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => setDebouncedTextFilters(next), 300);
  };

  if (loading && logs.length === 0) return <Loading text="Loading audit logs..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Audit Logs</h1>
      </div>

      <div className={styles.filters}>
        <select
          value={action}
          onChange={(e) => {
            setAction(e.target.value as AuditAction | "");
            setPage(0);
          }}
          className={styles.filterSelect}
          aria-label="Filter by action"
        >
          <option value="">All Actions</option>
          <option value="CREATE">CREATE</option>
          <option value="UPDATE">UPDATE</option>
          <option value="DELETE">DELETE</option>
        </select>
        <input
          type="text"
          placeholder="Entity"
          value={textFilters.entityName}
          onChange={(e) => handleTextFilterChange("entityName", e.target.value)}
          className={styles.filterInput}
          aria-label="Filter by entity"
        />
        <input
          type="number"
          placeholder="Entity ID"
          value={textFilters.entityId}
          onChange={(e) => handleTextFilterChange("entityId", e.target.value)}
          className={styles.filterInput}
          aria-label="Filter by entity id"
        />
        <input
          type="text"
          placeholder="User"
          value={textFilters.performedBy}
          onChange={(e) => handleTextFilterChange("performedBy", e.target.value)}
          className={styles.filterInput}
          aria-label="Filter by user"
        />
        <input
          type="datetime-local"
          aria-label="From date"
          value={start}
          onChange={(e) => {
            setStart(e.target.value);
            setPage(0);
          }}
          className={styles.filterInput}
        />
        <input
          type="datetime-local"
          aria-label="To date"
          value={end}
          onChange={(e) => {
            setEnd(e.target.value);
            setPage(0);
          }}
          className={styles.filterInput}
        />
      </div>

      {logs.length === 0 ? (
        <div className={styles.empty}>
          <h3>No audit logs found</h3>
        </div>
      ) : (
        <>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Action</th>
                  <th>Entity</th>
                  <th>Entity ID</th>
                  <th>Details</th>
                  <th>Performed By</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td data-label="ID">{log.id}</td>
                    <td data-label="Action">
                      <span className={styles.action}>{log.action}</span>
                    </td>
                    <td data-label="Entity">{log.entityName}</td>
                    <td data-label="Entity ID">{log.entityId ?? "—"}</td>
                    <td data-label="Details" className={styles.details}>
                      {log.details ?? "—"}
                    </td>
                    <td data-label="Performed By">{log.performedBy}</td>
                    <td data-label="Date">
                      {new Date(log.performedAt).toLocaleString()}
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
    </div>
  );
}
