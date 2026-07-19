import { useState, useEffect } from 'react'
import { useAuditLogs } from '../../../hooks/features/useAudit'
import Loading from '../../../components/UI/Loading/Loading'
import Pagination from '../../../components/UI/Pagination/Pagination'
import styles from './AdminAuditPage.module.css'

export default function AdminAuditPage() {
  const { logs, totalPages, loading, loadLogs } = useAuditLogs()
  const [page, setPage] = useState(0)

  useEffect(() => {
    loadLogs(page)
  }, [page])

  if (loading) return <Loading text="Loading audit logs..." />

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Audit Logs</h1>
      </div>

      {logs.length === 0 ? (
        <div className={styles.empty}>
          <h3>No audit logs found</h3>
        </div>
      ) : (
        <>
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
                  <td>{log.id}</td>
                  <td><span className={styles.action}>{log.action}</span></td>
                  <td>{log.entityName}</td>
                  <td>{log.entityId ?? '—'}</td>
                  <td className={styles.details}>{log.details ?? '—'}</td>
                  <td>{log.performedBy}</td>
                  <td>{new Date(log.performedAt).toLocaleString()}</td>
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
    </div>
  )
}