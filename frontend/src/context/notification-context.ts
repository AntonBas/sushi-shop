import { createContext } from "react";

export type NotificationType = "success" | "error" | "warning" | "info";

export interface NotificationItem {
  id: string;
  message: string;
  type: NotificationType;
  isVisible: boolean;
  duration?: number;
}

export interface NotificationContextType {
  notifications: NotificationItem[];
  showNotification: (message: string, type?: NotificationType, duration?: number) => string;
  hideNotification: (id: string) => void;
}

export const NotificationContext = createContext<NotificationContextType | null>(null);
