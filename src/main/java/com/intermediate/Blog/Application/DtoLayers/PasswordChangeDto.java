package com.intermediate.Blog.Application.DtoLayers;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDto {

    @NotNull
    private String oldPassword;

    @NotNull
    private String newPassword;


    public @NotNull String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(@NotNull String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public @NotNull String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(@NotNull String newPassword) {
        this.newPassword = newPassword;
    }
}
