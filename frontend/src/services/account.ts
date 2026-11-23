// Mock account service for the Account Information and Change Password screens.
import { isAxiosError } from "axios";
import api from "./api";
import { getCurrentUser } from "./users";
import type { User } from "../types";
export type SignInMethod = "password" | "google" | "apple";

export interface AccountProfile {
  id: number;
  name: string;
  username: string; // shown on ratings and reviews instead of the full name
  email: string;
  signInMethod: SignInMethod;
  photoUri: string | null;
}

export interface UpdateAccountInput {
  name: string;
  username: string;
}

const USE_MOCK = true;

// Change this to "google" or "apple" to see how the screens look for those users.
const MOCK_SIGN_IN_METHOD: SignInMethod = "password";

let mockProfile: AccountProfile = {
  id: 1,
  name: "Alex Smith",
  username: "alexsips",
  email: "alex@example.com",
  signInMethod: MOCK_SIGN_IN_METHOD,
  photoUri: null,
};

const delay = (ms = 500) => new Promise<void>((resolve) => setTimeout(resolve, ms));

const photoUrlFor = (path: string | null | undefined) =>
  path ? `${api.defaults.baseURL}${path}` : null;

// username and signInMethod come from the mock until the backend supports them.
const toProfile = (me: User): AccountProfile => ({
  ...mockProfile,
  id: me.id,
  name: me.name,
  email: me.email,
  photoUri: photoUrlFor(me.photoUrl),
});

function friendlyError(error: unknown, fallback: string): Error {
  if (isAxiosError(error)) {
    if (error.response) {
      return new Error(error.response.data?.message ?? fallback);
    }
    return new Error("Can't reach the server. Check your connection and try again.");
  }
  return new Error(fallback);
}

export async function getAccount(token: string): Promise<AccountProfile> {
  try {
    return toProfile(await getCurrentUser(token));
  } catch (error) {
    throw friendlyError(error, "Couldn't load your account.");
  }
}

export async function updateAccount(
  input: UpdateAccountInput,
): Promise<AccountProfile> {
  if (USE_MOCK) {
    await delay();
    mockProfile = {
      ...mockProfile,
      name: input.name,
      username: input.username,
    };
    return { ...mockProfile };
  }

  // TODO real: call updateUser(userId, { name }, token) from services/users.ts, plus
  // whatever endpoint saves the username.
  throw new Error("Not connected to the backend yet.");
}

export async function updatePhoto(
  token: string,
  photoUri: string | null,
): Promise<AccountProfile> {
  const headers = { Authorization: `Bearer ${token}` };
  try {
    if (photoUri) {
      const form = new FormData();
      form.append("photo", {
        uri: photoUri,
        name: "photo.jpg",
        type: "image/jpeg",
      } as unknown as Blob);
      const { data } = await api.post<User>("/api/users/me/photo", form, {
        headers: { ...headers, "Content-Type": "multipart/form-data" },
      });
      return toProfile(data);
    }
    const { data } = await api.delete<User>("/api/users/me/photo", { headers });
    return toProfile(data);
  } catch (error) {
    throw friendlyError(error, "Couldn't save your photo.");
  }
}

export async function changePassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  if (USE_MOCK) {
    await delay();
    // Type "wrong" as the current password to test the error message.
    if (currentPassword === "wrong") {
      throw new Error("Your current password is incorrect.");
    }
    return;
  }

  // TODO real: ask the backend for a change-password endpoint that checks the
  // current password before saving the new one.
  void newPassword;
  throw new Error("Not connected to the backend yet.");
}

export async function deleteAccount(): Promise<void> {
  if (USE_MOCK) {
    await delay();
    return;
  }

  // TODO real: call the backend's delete-account endpoint, then clear the saved
  // login (token) so the app treats the user as signed out.
  throw new Error("Not connected to the backend yet.");
}