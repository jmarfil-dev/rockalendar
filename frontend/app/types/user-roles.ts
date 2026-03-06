export type Role = "ROLE_USER" | "ROLE_MODERATOR" | "ROLE_ADMIN";

export type Rule = {
  prefix: string;
  auth: boolean;
  anyOfRoles?: Role[]; // exige al menos uno
};
