import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import { useNotification } from "../../../context/useNotification";
import { useApi } from "../../../hooks/common/useApi";
import * as usersApi from "../../../api/user";
import Button from "../../../components/UI/Button/Button";
import Input from "../../../components/UI/Input/Input";
import type { UserResponse } from "../../../types";
import styles from "./ProfilePage.module.css";

type Tab = "profile" | "address" | "security";

function tabFromPath(pathname: string): Tab {
  if (pathname.endsWith("/address")) return "address";
  if (pathname.endsWith("/security")) return "security";
  return "profile";
}

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const { showNotification } = useNotification();
  const updateApi = useApi<UserResponse>();
  const passwordApi = useApi<void>();
  const emailApi = useApi<void>();
  const location = useLocation();

  const activeTab = tabFromPath(location.pathname);
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [city, setCity] = useState("");
  const [street, setStreet] = useState("");
  const [house, setHouse] = useState("");
  const [apartment, setApartment] = useState("");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [newEmail, setNewEmail] = useState("");

  const [syncedUser, setSyncedUser] = useState<UserResponse | null>(null);

  if (user && user !== syncedUser) {
    setSyncedUser(user);
    setName(user.name);
    setPhone(user.phone);
    setCity(user.address?.city || "");
    setStreet(user.address?.street || "");
    setHouse(user.address?.house || "");
    setApartment(user.address?.apartment || "");
  }

  const handleUpdateProfile = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await updateApi.execute(() =>
        usersApi.updateProfile({
          name: name || undefined,
          phone: phone || undefined,
        }),
      );
      await refreshUser();
      showNotification("Profile updated", "success");
    } catch {
      return;
    }
  };

  const handleUpdateAddress = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await updateApi.execute(() =>
        usersApi.updateProfile({
          address: city
            ? { city, street, house, apartment: apartment || undefined }
            : undefined,
        }),
      );
      await refreshUser();
      showNotification("Address updated", "success");
    } catch {
      return;
    }
  };

  const handleClearAddress = async () => {
    try {
      await updateApi.execute(() => usersApi.clearAddress());
      await refreshUser();
      showNotification("Address removed", "success");
    } catch {
      return;
    }
  };

  const handleRequestEmailChange = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await emailApi.execute(() => usersApi.requestEmailChange({ newEmail }));
    } catch {
      return;
    }
    await refreshUser();
    setNewEmail("");
    showNotification(`Confirmation link sent to ${newEmail}`, "success");
  };

  const handleChangePassword = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      showNotification("New passwords do not match", "error");
      return;
    }
    try {
      await passwordApi.execute(() =>
        usersApi.changePassword({ oldPassword, newPassword }),
      );
    } catch {
      return;
    }
    setOldPassword("");
    setNewPassword("");
    setConfirmPassword("");
    showNotification("Password changed", "success");
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>My Profile</h1>
      <div className={styles.tabs}>
        <Link
          to="/profile"
          className={`${styles.tab} ${activeTab === "profile" ? styles.active : ""}`}
        >
          Profile
        </Link>
        <Link
          to="/profile/address"
          className={`${styles.tab} ${activeTab === "address" ? styles.active : ""}`}
        >
          Address
        </Link>
        <Link
          to="/profile/security"
          className={`${styles.tab} ${activeTab === "security" ? styles.active : ""}`}
        >
          Security
        </Link>
      </div>
      {activeTab === "profile" && (
        <form onSubmit={(e) => void handleUpdateProfile(e)} className={styles.form}>
          <div className={styles.fieldReadonly}>
            <label htmlFor="profile-email" className={styles.label}>Email</label>
            <input
              id="profile-email"
              value={user?.email || ""}
              disabled
              className={styles.input}
            />
          </div>
          <Input
            label="Name"
            name="name"
            autoComplete="name"
            value={name}
            onChange={setName}
            placeholder="Your name"
          />
          <Input
            label="Phone"
            name="phone"
            autoComplete="tel"
            value={phone}
            onChange={setPhone}
            placeholder="+380991234567"
          />
          <Button type="submit" loading={updateApi.loading}>
            Save
          </Button>
        </form>
      )}
      {activeTab === "address" && (
        <form onSubmit={(e) => void handleUpdateAddress(e)} className={styles.form}>
          <Input
            label="City"
            name="city"
            autoComplete="address-level2"
            value={city}
            onChange={setCity}
            placeholder="City"
          />
          <Input
            label="Street"
            name="street"
            autoComplete="address-line1"
            value={street}
            onChange={setStreet}
            placeholder="Street"
          />
          <div className={styles.row}>
            <Input
              label="House"
              name="house"
              value={house}
              onChange={setHouse}
              placeholder="House"
            />
            <Input
              label="Apartment"
              name="apartment"
              value={apartment}
              onChange={setApartment}
              placeholder="Apt"
            />
          </div>
          <Button type="submit" loading={updateApi.loading}>
            Save Address
          </Button>
          {user?.address && (
            <Button type="button" variant="secondary" onClick={() => void handleClearAddress()} disabled={updateApi.loading}>
              Remove Address
            </Button>
          )}
        </form>
      )}
      {activeTab === "security" && (
        <>
          <h2 className={styles.sectionTitle}>Change Password</h2>
          {user?.hasPassword ? (
            <form onSubmit={(e) => void handleChangePassword(e)} className={styles.form}>
              <Input
                label="Current Password"
                name="currentPassword"
                autoComplete="current-password"
                type="password"
                value={oldPassword}
                onChange={setOldPassword}
                placeholder="••••••••"
              />
              <Input
                label="New Password"
                name="newPassword"
                autoComplete="new-password"
                type="password"
                value={newPassword}
                onChange={setNewPassword}
                placeholder="Min 8 characters"
              />
              <Input
                label="Confirm New Password"
                name="confirmPassword"
                autoComplete="new-password"
                type="password"
                value={confirmPassword}
                onChange={setConfirmPassword}
                placeholder="Repeat new password"
              />
              <Button type="submit" loading={passwordApi.loading}>
                Change Password
              </Button>
            </form>
          ) : (
            <p className={styles.pendingNotice}>
              This account signs in with Google and has no password yet.{" "}
              <Link to="/forgot-password">Set a password</Link> to also sign in with email.
            </p>
          )}

          <h2 className={styles.sectionTitle}>Change Email</h2>
          <form onSubmit={(e) => void handleRequestEmailChange(e)} className={styles.form}>
            {user?.pendingEmail && (
              <p className={styles.pendingNotice}>
                Confirmation pending for <strong>{user.pendingEmail}</strong>. Check your inbox to complete the change.
              </p>
            )}
            <Input
              label="New Email"
              name="newEmail"
              autoComplete="email"
              type="email"
              value={newEmail}
              onChange={setNewEmail}
              placeholder="new@example.com"
            />
            <Button type="submit" loading={emailApi.loading} disabled={!newEmail}>
              Send Confirmation Link
            </Button>
          </form>
        </>
      )}
    </div>
  );
}
