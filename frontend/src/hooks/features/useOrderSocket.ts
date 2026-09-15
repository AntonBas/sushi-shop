import { useEffect, useEffectEvent, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { issueWsTicket } from "../../api/auth";
import { API_BASE_URL } from "../../config/env";
import { useNotification } from "../../context/useNotification";

const WATCHDOG_TIMEOUT_MS = 15000;
const RECONNECT_DELAY_MS = 5000;

export function useOrderSocket(onConnect: (client: Client) => void) {
  const stompRef = useRef<Client | null>(null);
  const connectionIssueNotifiedRef = useRef(false);
  const onConnectEvent = useEffectEvent(onConnect);
  const { showNotification } = useNotification();

  useEffect(() => {
    const notifyConnectionIssue = () => {
      if (connectionIssueNotifiedRef.current) return;
      connectionIssueNotifiedRef.current = true;
      showNotification(
        "Live order updates unavailable, reconnecting...",
        "warning",
      );
    };

    let watchdogId: number | null = null;
    const clearWatchdog = () => {
      if (watchdogId !== null) {
        window.clearTimeout(watchdogId);
        watchdogId = null;
      }
    };
    const armWatchdog = (client: Client) => {
      clearWatchdog();
      watchdogId = window.setTimeout(() => {
        notifyConnectionIssue();
        client.deactivate().then(() => client.activate());
      }, WATCHDOG_TIMEOUT_MS);
    };

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      beforeConnect: async (client) => {
        const ticket = await issueWsTicket();
        client.connectHeaders = { Authorization: `Bearer ${ticket}` };
        armWatchdog(client);
      },
      reconnectDelay: RECONNECT_DELAY_MS,
      onConnect: () => {
        clearWatchdog();
        connectionIssueNotifiedRef.current = false;
        onConnectEvent(client);
      },
      onStompError: (frame) => {
        clearWatchdog();
        console.error("WebSocket STOMP error:", frame.headers.message);
        notifyConnectionIssue();
      },
      onWebSocketError: (event) => {
        clearWatchdog();
        console.error("WebSocket connection error:", event);
        notifyConnectionIssue();
      },
      onWebSocketClose: () => clearWatchdog(),
    });
    client.activate();
    stompRef.current = client;

    return () => {
      clearWatchdog();
      client.deactivate();
      stompRef.current = null;
    };
  }, [showNotification]);

  return stompRef;
}
