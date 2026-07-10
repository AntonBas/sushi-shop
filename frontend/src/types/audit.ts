export interface AuditLogResponse {
  id: number
  action: string
  entityName: string
  entityId: number | null
  details: string | null
  performedBy: string
  performedAt: string
}

export interface AuditLogFilters {
  action?: string
  entityName?: string
  entityId?: number
  performedBy?: string
  start?: string
  end?: string
}