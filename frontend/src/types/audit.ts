export interface AuditLogResponse {
  id: number
  action: string
  entityName: string
  entityId: number | null
  details: string | null
  performedBy: string
  performedAt: string
}