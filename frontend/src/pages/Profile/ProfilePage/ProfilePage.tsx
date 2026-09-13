import { useState, useEffect } from "react";
import { useAuth } from "../../../context/useAuth";
import { useNotification } from "../../../context/useNotification";
import { useApi } from "../../../hooks/common/useApi";
import * as usersApi from "../../../api/user";
import Button from "../../../components/UI/Button/Button";
import Input from "../../../components/UI/Input/Input";
import type { UserResponse } from "../../../types";
import styles from "./ProfilePage.module.css";

type Tab = "profile" | "address" | "password";

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const { showNotification } = useNotification();
  const updateApi = useApi<UserResponse>();
  const passwordApi = useApi<void>();

  const [activeTab, setActiveTab] = useState<Tab>("profile");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [city, setCity] = useState("");
  const [street, setStreet] = useState("");
  const [house, setHouse] = useState("");
  const [apartment, setApartment] = useState("");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

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

  const showError = (err: unknown, fallback: string) => {
    const message =
      (err as { response?: { data?: { message?: string } } })?.response?.data
        ?.message || fallback;
    showNotification(message, "error");
  };

  const handleUpdateProfile = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      const updated = await updateApi.execute(() =>
        usersApi.updateProfile({
          name: name || undefined,
          phone: phone || undefined,
        }),
      );
      if (updated) {
        await refreshUser();
        showNotification("Profile updated", "success");
      }
    } catch (err: unknown) {
      showError(err, "Failed to update profile");
    }
  };

  const handleUpdateAddress = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      const updated = await updateApi.execute(() =>
        usersApi.updateProfile({
          address: city
            ? { city, street, house, apartment: apartment || undefined }
            : undefined,
        }),
      );
      if (updated) {
        await refreshUser();
        showNotification("Address updated", "success");
      }
    } catch (err: unknown) {
      showError(err, "Failed to update address");
    }
  };

  const handleChangePassword = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await passwordApi.execute(() =>
        usersApi.changePassword({ oldPassword, newPassword }),
      );
      setOldPassword("");
      setNewPassword("");
      showNotification("Password changed", "success");
    } catch (err: unknown) {
      showError(err, "Failed to change password");
    }
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>My Profile</h1>
      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === "profile" ? styles.active : ""}`}
          onClick={() => setActiveTab("profile")}
        >
          Profile
        </button>
        <button
          className={`${styles.tab} ${activeTab === "address" ? styles.active : ""}`}
          onClick={() => setActiveTab("address")}
        >
          Address
        </button>
        <button
          className={`${styles.tab} ${activeTab === "password" ? styles.active : ""}`}
          onClick={() => setActiveTab("password")}
        >
          Password
        </button>
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
      {activeTab === "password" && (
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
          <Button type="submit" loading={passwordApi.loading}>
            Change Password
          </Button>
        </form>
      )}
    </div>
  );
}
