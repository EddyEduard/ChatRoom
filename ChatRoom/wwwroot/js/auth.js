$(document).ready(function () {
    const username = $("#username");
    const email = $("#email");
    const password = $("#password");
    const rememberMe = $("#remember-me");
    const signIn = $("#sign-in");
    const signUp = $("#sign-up");
    const loading = $("#loading");

    // Set the remembered credentials.

    const rememberedEmail = getCookie("remembered_email");
    const rememberedPassword = getCookie("remembered_password");

    if (rememberedEmail != null && rememberedPassword != null) {
        email.val(rememberedEmail);
        password.val(atob(rememberedPassword));
    }

    // Sign in form validation.

    $("#sign-in-form").validate({
        rules: {
            email: {
                required: true,
                email: true
            },
            password: {
                required: true,
                minlength: 6
            }
        },
        submitHandler: function () {
            const user = { email: email.val(), password: password.val() };

            signIn.hide();
            loading.show();

            // Request sign in.

            $.ajax({
                type: "POST",
                url: `${window.location.origin}/api/auth/login`,
                contentType: "application/json",
                cache: false,
                data: JSON.stringify(user),
                success: function (response) {
                    setCookie(response.expire_date, "token", response.token);

                    if (rememberMe.val() == "on") {
                        const date = new Date();
                        date.setFullYear(2050);

                        setCookie(date.getTime(), "remembered_email", user.email);
                        setCookie(date.getTime(), "remembered_password", btoa(user.password));
                    } else {
                        deleteCookie("remembered_email");
                        deleteCookie("remembered_password");
                    }

                    loading.hide();
                    signIn.show();

                    window.location.href = "/account/chat";
                },
                error: function (error) {
                    console.log(error);

                    loading.hide();
                    signIn.show();
                }
            });
        }
    });

    // Sign up form validation.

    $("#sign-up-form").validate({
        rules: {
            username: {
                required: true,
                minlength: 6
            },
            email: {
                required: true,
                email: true
            },
            password: {
                required: true,
                minlength: 6
            }
        },
        submitHandler: function () {
            const user = { name: username.val(), email: email.val(), password: password.val() };

            signUp.hide();
            loading.show();

            // Request sign up.

            $.ajax({
                type: "POST",
                url: `${window.location.origin}/api/auth/register`,
                contentType: "application/json",
                cache: false,
                data: JSON.stringify(user),
                success: function (response) {
                    setCookie(response.expire_date, "token", response.token);

                    if (rememberMe.val() == "on") {
                        const date = new Date();
                        date.setFullYear(2050);

                        setCookie(date.getTime(), "remembered_email", user.email);
                        setCookie(date.getTime(), "remembered_password", btoa(user.password));
                    } else {
                        deleteCookie("remembered_email");
                        deleteCookie("remembered_password");
                    }

                    loading.hide();
                    signUp.show();

                    window.location.href = "/auth/login";
                },
                error: function (error) {
                    console.log(error);

                    loading.hide();
                    signUp.show();
                }
            });
        }
    });
});