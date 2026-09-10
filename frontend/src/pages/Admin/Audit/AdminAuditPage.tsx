import { useState, useEffect } from "react";
import { useAuditLogs } from "../../../hooks/features/useAudit";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import type { AuditAction } from "../../../types";
import styles from "./AdminAuditPage.module.css";

export default function AdminAuditPage() {
  const { logs, totalPages, loading, loadLogs } = useAuditLogs();
  const [page, setPage] = useState(0);
  const [action, setAction] = useState<AuditAction | "">("");
  const [entityName, setEntityName] = useState("");
  const [entityId, setEntityId] = useState("");
  const [performedBy, setPerformedBy] = useState("");
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");

  useEffect(() => {
    loadLogs(page, {
      action: action || undefined,
      entityName: entityName || undefined,
      entityId: entityId ? Number(entityId) : undefined,
      performedBy: performedBy || undefined,
      start: start || undefined,
      end: end || undefined,
    });
  }, [page, action, entityName, entityId, performedBy, start, end]);

  if (loading) return <Loading text="Loading audit logs..." />;

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
          <option value="LOGIN">LOGIN</option>
          <option value="LOGOUT">LOGOUT</option>
          <option value="EXPORT">EXPORT</option>
        </select>
        <input
          type="text"
          placeholder="Entity"
          value={entityName}
          onChange={(e) => {
            setEntityName(e.target.value);
            setPage(0);
          }}
          className={styles.filterInput}
        />
        <input
          type="number"
          placeholder="Entity ID"
          value={entityId}
          onChange={(e) => {
            setEntityId(e.target.value);
            setPage(0);
          }}
          className={styles.filterInput}
        />
        <input
          type="text"
          placeholder="User"
          value={performedBy}
          onChange={(e) => {
            setPerformedBy(e.target.value);
            setPage(0);
          }}
          className={styles.filterInput}
        />
        <input
          type="datetime-local"
          value={start}
          onChange={(e) => {
            setStart(e.target.value);
            setPage(0);
          }}
          className={styles.filterInput}
        />
        <input
          type="datetime-local"
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
