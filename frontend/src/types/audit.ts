import type { AuditAction } from './enums'


export interface AuditLogResponse {
  id: number
  action: AuditAction
  entityName: string
  entityId: number | null
  details: string | null
  performedBy: string
  performedAt: string
}

export interface AuditLogFilters {
  action?: AuditAction
  entityName?: string
  entityId?: number
  performedBy?: string
  start?: string
  end?: string
}