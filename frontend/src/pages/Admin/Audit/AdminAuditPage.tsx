import { useEffect } from "react";
import { useAuditLogs } from "../../../hooks/features/useAudit";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import { AUDIT_ACTIONS } from "../../../types/enums";
import { pickAllowed, useDebouncedParamInput, useListSearchParams } from "../../../hooks/common/useListSearchParams";
import styles from "./AdminAuditPage.module.css";

const DATE_TIME_LOCAL = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/;

function validDateTimeLocal(value: string): string {
  return DATE_TIME_LOCAL.test(value) && !Number.isNaN(new Date(value).getTime()) ? value : "";
}

export default function AdminAuditPage() {
  const { logs, totalPages, loading, loadLogs } = useAuditLogs();
  const { page, getParam, updateParams, setPage } = useListSearchParams();
  const action = pickAllowed(getParam("action"), AUDIT_ACTIONS);
  const entityName = getParam("entity");
  const entityId = /^\d+$/.test(getParam("entityId")) ? getParam("entityId") : "";
  const performedBy = getParam("user");
  const start = validDateTimeLocal(getParam("from"));
  const end = validDateTimeLocal(getParam("to"));
  const [entityNameInput, handleEntityNameChange] = useDebouncedParamInput(entityName, (value) =>
    updateParams({ entity: value }, { replace: true }),
  );
  const [entityIdInput, handleEntityIdChange] = useDebouncedParamInput(entityId, (value) =>
    updateParams({ entityId: value }, { replace: true }),
  );
  const [performedByInput, handlePerformedByChange] = useDebouncedParamInput(performedBy, (value) =>
    updateParams({ user: value }, { replace: true }),
  );

  useEffect(() => {
    void loadLogs(page, {
      action: action || undefined,
      entityName: entityName || undefined,
      entityId: entityId ? Number(entityId) : undefined,
      performedBy: performedBy || undefined,
      start: start ? new Date(start).toISOString() : undefined,
      end: end ? new Date(end).toISOString() : undefined,
    });
  }, [page, action, entityName, entityId, performedBy, start, end, loadLogs]);

  if (loading && logs.length === 0) return <Loading text="Loading audit logs..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Audit Logs</h1>
      </div>

      <div className={styles.filters}>
        <select
          value={action}
          onChange={(e) => updateParams({ action: e.target.value })}
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
          value={entityNameInput}
          onChange={(e) => handleEntityNameChange(e.target.value)}
          className={styles.filterInput}
          aria-label="Filter by entity"
        />
        <input
          type="number"
          placeholder="Entity ID"
          value={entityIdInput}
          onChange={(e) => handleEntityIdChange(e.target.value)}
          className={styles.filterInput}
          aria-label="Filter by entity id"
        />
        <input
          type="text"
          placeholder="User"
          value={performedByInput}
          onChange={(e) => handlePerformedByChange(e.target.value)}
          className={styles.filterInput}
          aria-label="Filter by user"
        />
        <input
          type="datetime-local"
          aria-label="From date"
          value={start}
          onChange={(e) => updateParams({ from: e.target.value }, { replace: true })}
          className={styles.filterInput}
        />
        <input
          type="datetime-local"
          aria-label="To date"
          value={end}
          onChange={(e) => updateParams({ to: e.target.value }, { replace: true })}
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
