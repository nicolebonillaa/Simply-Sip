// Mock account service for the Account Information and Change Password screens.

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

export async function getAccount(): Promise<AccountProfile> {
  if (USE_MOCK) {
    await delay(300);
    return { ...mockProfile };
  }

  // TODO real: get the logged-in user's token and call getCurrentUser(token) from
  // services/users.ts, then map the result to AccountProfile. The backend does not
  // return username, signInMethod, or photoUri yet.
  throw new Error("Not connected to the backend yet.");
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
  photoUri: string | null,
): Promise<AccountProfile> {
  if (USE_MOCK) {
    await delay(300);
    mockProfile = { ...mockProfile, photoUri };
    return { ...mockProfile };
  }

  // TODO real: send the image to the backend as multipart form data
  // (for example POST /api/users/me/photo) and return the updated profile.
  throw new Error("Not connected to the backend yet.");
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