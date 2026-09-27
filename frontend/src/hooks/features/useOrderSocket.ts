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

    let retryId: number | null = null;
    const clearRetry = () => {
      if (retryId !== null) {
        window.clearTimeout(retryId);
        retryId = null;
      }
    };

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      beforeConnect: async (client) => {
        try {
          const ticket = await issueWsTicket();
          client.connectHeaders = { Authorization: `Bearer ${ticket}` };
          armWatchdog(client);
        } catch {
          notifyConnectionIssue();
          await client.deactivate();
          clearRetry();
          retryId = window.setTimeout(() => client.activate(), RECONNECT_DELAY_MS);
        }
      },
      reconnectDelay: RECONNECT_DELAY_MS,
      onConnect: () => {
        clearWatchdog();
        connectionIssueNotifiedRef.current = false;
        onConnectEvent(client);
      },
      onStompError: () => {
        clearWatchdog();
        notifyConnectionIssue();
      },
      onWebSocketError: () => {
        clearWatchdog();
        notifyConnectionIssue();
      },
      onWebSocketClose: () => clearWatchdog(),
    });
    client.activate();
    stompRef.current = client;

    return () => {
      clearWatchdog();
      clearRetry();
      client.deactivate();
      stompRef.current = null;
    };
  }, [showNotification]);

  return stompRef;
}
