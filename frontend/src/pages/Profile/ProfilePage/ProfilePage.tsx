import { useState, useEffect } from "react";
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

  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    if (user) {
      setName(user.name);
      setPhone(user.phone);
      setCity(user.address?.city || "");
      setStreet(user.address?.street || "");
      setHouse(user.address?.house || "");
      setApartment(user.address?.apartment || "");
    }
  }, [user]);
  /* eslint-enable react-hooks/set-state-in-effect */

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
        <form onSubmit={handleUpdateProfile} className={styles.form}>
          <div className={styles.fieldReadonly}>
            <label className={styles.label}>Email</label>
            <input
              value={user?.email || ""}
              disabled
              className={styles.input}
            />
          </div>
          <Input
            label="Name"
            value={name}
            onChange={setName}
            placeholder="Your name"
          />
          <Input
            label="Phone"
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
        <form onSubmit={handleUpdateAddress} className={styles.form}>
          <Input
            label="City"
            value={city}
            onChange={setCity}
            placeholder="City"
          />
          <Input
            label="Street"
            value={street}
            onChange={setStreet}
            placeholder="Street"
          />
          <div className={styles.row}>
            <Input
              label="House"
              value={house}
              onChange={setHouse}
              placeholder="House"
            />
            <Input
              label="Apartment"
              value={apartment}
              onChange={setApartment}
              placeholder="Apt"
            />
          </div>
          <Button type="submit" loading={updateApi.loading}>
            Save Address
          </Button>
        </form>
      )}
      {activeTab === "security" && (
        <>
          <h2 className={styles.sectionTitle}>Change Password</h2>
          <form onSubmit={handleChangePassword} className={styles.form}>
            <Input
              label="Current Password"
              type="password"
              value={oldPassword}
              onChange={setOldPassword}
              placeholder="••••••••"
            />
            <Input
              label="New Password"
              type="password"
              value={newPassword}
              onChange={setNewPassword}
              placeholder="Min 8 characters"
            />
            <Input
              label="Confirm New Password"
              type="password"
              value={confirmPassword}
              onChange={setConfirmPassword}
              placeholder="Repeat new password"
            />
            <Button type="submit" loading={passwordApi.loading}>
              Change Password
            </Button>
          </form>

          <h2 className={styles.sectionTitle}>Change Email</h2>
          <form onSubmit={handleRequestEmailChange} className={styles.form}>
            {user?.pendingEmail && (
              <p className={styles.pendingNotice}>
                Confirmation pending for <strong>{user.pendingEmail}</strong>. Check your inbox to complete the change.
              </p>
            )}
            <Input
              label="New Email"
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
