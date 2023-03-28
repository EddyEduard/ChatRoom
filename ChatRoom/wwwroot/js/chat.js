const connection = new signalR.HubConnectionBuilder().withUrl("/chat").build();
const token = getCookie("token");

// Connect to socket.

connection.start();

// Message constructor.

function Message(idFrom, idTo, content, status, dateTime, user, type, seenMembers = "") {
    this.idFrom = idFrom;
    this.idTo = idTo;
    this.content = content;
    this.status = status;
    this.dateTime = dateTime;
    this.user = user;
    this.type = type;
    this.seenMembers = seenMembers;
}

// Actions and effects.

$(document).ready(function () {
    $("#dropdown-settings-contact").click(function () {
        if ($("#settings-contact").hasClass("show"))
            $("#settings-contact").removeClass("show");
        else
            $("#settings-contact").addClass("show");
    });

    $("#dropdown-settings-group").click(function () {
        if ($("#settings-group").hasClass("show"))
            $("#settings-group").removeClass("show");
        else
            $("#settings-group").addClass("show");
    });

    $(document).click(function (event) {
        if (event.target.id != "dropdown-settings-contact")
            $("#settings-contact").removeClass("show");

        if (event.target.id != "dropdown-settings-group")
            $("#settings-group").removeClass("show");
    });

    setInterval(() => {
        $(".wrap").css({ "visibility": "visible" });
        $(".search-groups").css({ "visibility": "visible" });
        $(".create").css({ "visibility": "visible" });
        $(".bottom-bar").css({ "visibility": "visible" });
        $(".items").css({ "visibility": "visible" });
        $(".contact-profile").css({ "visibility": "visible" });
        $(".messages").css({ "visibility": "visible" });
        $(".message-input").css({ "visibility": "visible" });
    }, 500);
});

angular.module("ChatRoom", [])
    .controller("ProfileController", function ($scope) {
        $scope.isLoadedProfile = false;
        $scope.isEditingProfileUser = false;
        $scope.isEditingProfileGroup = false;
        $scope.isSaveingProfile = false;

        // Get profile user.

        $.ajax({
            type: "GET",
            url: `${window.location.origin}/api/account`,
            responseType: "application/json",
            headers: {
                "Authorization": "Bearer " + token
            },
            success: function (response) {
                $scope.$root.profile = response;
                $scope.$digest();

                setTimeout(() => {
                    $scope.isLoadedProfile = true;
                    $scope.$digest();
                }, 1000);
            },
            error: function () {
                alert("Failed to load profile.user.");
            }
        })

        $scope.$root.createGroup = function (name) {
            if (name != undefined && name != null && name.length > 0) {

                // Create a new group.

                $.ajax({
                    type: "POST",
                    url: `${window.location.origin}/api/relationship/group`,
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    contentType: "application/json",
                    cache: false,
                    data: JSON.stringify({ name: name }),
                    success: function (group) {
                        $scope.$root.profile.groups.push(group);
                        $scope.$root.groups.push(group);
                        $scope.$root.$digest();
                    },
                    error: function (error) {
                        alert(error.responseJSON.message);
                    }
                });
            }
        };

        $scope.$root.editProfileUser = function () {
            $scope.isEditingProfileUser = true;
        };

        $scope.$root.editProfileGroup = function () {
            $scope.isEditingProfileGroup = true;
        };

        $scope.$root.saveEditProfileUserOrGroup = function () {
            const image = $("#profile-image")[0];
            const name = $("#profile-name").val();
            const email = $("#profile-email").val();
            
            if ($scope.isEditingProfileUser) {
                $scope.isSaveingProfile = true;

                const uploadProfileUserImage = new Promise((resolve, reject) => {
                    if (image.files.length > 0) {
                        const formData = new FormData();
                        formData.append("image", image.files[0]);

                        // Edit profile image.

                        $.ajax({
                            type: "PUT",
                            url: `${window.location.origin}/api/account/profile-image`,
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            processData: false,
                            contentType: false,
                            data: formData,
                            success: function (imageUrl) {
                                $scope.$root.profile.user.image = imageUrl;
                                $scope.$root.$digest();
                                resolve();
                            },
                            error: function (error) {
                                reject(error.responseJSON.message);
                            }
                        });
                    } else
                        resolve();
                });

                const updateProfileUser = new Promise((resolve, reject) => {

                    // Edit profile user.

                    $.ajax({
                        type: "PUT",
                        url: `${window.location.origin}/api/account`,
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        contentType: "application/json",
                        cache: false,
                        data: JSON.stringify({ name: name, email: email }),
                        success: function () {
                            $scope.$root.profile.user.name = name;
                            $scope.$root.profile.user.email = email;
                            $scope.$root.$digest();
                            resolve();
                        },
                        error: function (error) {
                            reject(error.responseJSON.message);
                        }
                    });
                });

                Promise.all([uploadProfileUserImage, updateProfileUser]).then(() => {
                    $scope.isSaveingProfile = false;
                    $scope.isEditingProfileUser = false;
                    $scope.$digest();
                }).catch(error => alert(error));
            } else if ($scope.isEditingProfileGroup) {
                $scope.isSaveingProfile = true;

                const uploadProfileGroupImage = new Promise((resolve, reject) => {
                    if (image.files.length > 0) {
                        const formData = new FormData();
                        formData.append("image", image.files[0]);

                        // Edit profile group image.

                        $.ajax({
                            type: "PUT",
                            url: `${window.location.origin}/api/relationship/group/${$scope.$root.manageGroup.id}/profile-image`,
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            processData: false,
                            contentType: false,
                            data: formData,
                            success: function (imageUrl) {
                                $scope.$root.manageGroup.image = imageUrl;
                                $scope.$root.$digest();
                                resolve();
                            },
                            error: function (error) {
                                reject(error.responseJSON.message);
                            }
                        });
                    } else
                        resolve();
                });

                const updateProfileGroup = new Promise((resolve, reject) => {

                    // Edit profile group.

                    $.ajax({
                        type: "PUT",
                        url: `${window.location.origin}/api/relationship/group/${$scope.$root.manageGroup.id}`,
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        contentType: "application/json",
                        cache: false,
                        data: JSON.stringify({ name: name }),
                        success: function () {
                            $scope.$root.manageGroup.name = name;
                            $scope.$root.$digest();
                            resolve();
                        },
                        error: function (error) {
                            reject(error.responseJSON.message);
                        }
                    });
                });

                Promise.all([uploadProfileGroupImage, updateProfileGroup]).then(() => {
                    $scope.isSaveingProfile = false;
                    $scope.isEditingProfileGroup = false;
                    $scope.$digest();
                }).catch(error => alert(error));
            }
        };

        $scope.$root.cancelEditProfileUserOrGroup = function () {
            $scope.isEditingProfileUser = false;
            $scope.isEditingProfileGroup = false;
        };

        $scope.$root.closeManageProfileUserOrGroup = function () {
            $scope.isEditingProfileUser = false;
            $scope.isEditingProfileGroup = false;
            $scope.$root.manageGroup = undefined;

            $("#manage-profile-user-or-group").toggle("right");
        };
    })
    .controller("ContactController", function ($scope) {
        $scope.$root.isOpenContactList = false;
        $scope.$root.isOpenGroupList = false;
        $scope.$root.isOpenMemberList = false;
        $scope.$root.isManageProfileUser = false;
        $scope.$root.isManageProfileGroup = false;
        $scope.$root.isLoadedContacts = false;
        $scope.$root.isLoadedMembers = false;

        // Get all contacts and groups.

        $.ajax({
            type: "GET",
            url: `${window.location.origin}/api/relationship/contacts`,
            responseType: "application/json",
            headers: {
                "Authorization": "Bearer " + token
            },
            success: function (response) {
                $scope.$root.users = response.users;
                $scope.$root.groups = response.groups;
                $scope.$digest();

                connection.on("OnlineUsers", function (users) {
                    for (const user of $scope.users)
                        users.indexOf(user.id) != -1 ? user.status = "ONLINE" : user.status = "OFFLINE";
                    $scope.$digest();
                });

                setTimeout(() => {
                    connection.invoke("ConnectUser", $scope.profile.user.id);
                }, 1000);

                connection.on("OnlineGroups", function (groups) {
                    for (const group of $scope.groups)
                        groups.indexOf(group.id) != -1 ? group.status = "ONLINE" : group.status = "OFFLINE";
                    $scope.$digest();
                });

                setTimeout(() => {
                    for (const group of $scope.groups)
                        connection.invoke("ConnectGroup", group.id);
                }, 1000);

                setTimeout(() => {
                    $scope.$root.isLoadedContacts = true;
                    $scope.$root.$digest();
                }, 1000);
            },
            error: function () {
                alert("Failed to load contacts.");
            }
        });

        $scope.openConversation = function (id, type) {
            if(type == "user")
                $scope.$root.userConversation = $scope.users.find(x => x.id == id);
            else if (type == "group")
                $scope.$root.groupConversation = $scope.groups.find(x => x.id == id);

            $scope.$root.isOpenedConversation = true;
            $scope.$root.typeConversation = type;
            $scope.$root.$broadcast("loadMessagesFromConversation");
        };

        $scope.addContact = function (id) {
            $scope.$root.userConversation = $scope.userList.find(x => x.id == id);

            $scope.$root.typeConversation = "user";
            $scope.$root.isOpenedConversation = true;
            $scope.$root.isOpenContactList = false;
            $scope.$root.isOpenGroupList = false;

            $scope.$root.$broadcast("loadMessagesFromConversation");
        };

        $scope.openContactList = function (type) {
            if (type == "my") {
                $scope.$root.isOpenContactList = false;
                $scope.$root.isOpenGroupList = false;
            } else {
                $scope.$root.isLoadedContacts = false;

                // Get all users.

                $.ajax({
                    type: "GET",
                    url: `${window.location.origin}/api/relationship/users`,
                    responseType: "application/json",
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function (response) {
                        $scope.$root.userList = response.filter(x => x.id != $scope.profile.user.id && $scope.users.find(y => y.id == x.id) == null);
                        $scope.$root.isOpenContactList = true;
                        $scope.$root.isOpenGroupList = false;
                        $scope.$root.isLoadedContacts = true;
                        $scope.$root.$digest();
                    },
                    error: function () {
                        alert("Failed to load users.");
                    }
                });
            }
        }

        $scope.openSettings = function (type) {
            $("#settings").removeClass("show");

            if (type == "PROFILE") {
                if ($scope.$root.isManageProfileGroup == false)
                    $("#manage-profile-user-or-group").toggle("right");

                $scope.$root.isOpenContactList = false;
                $scope.$root.isOpenGroupList = false;
                $scope.$root.isManageProfileGroup = false;
                $scope.$root.isManageProfileUser = true;
            } else if (type == "GROUPS") {
                if ($scope.$root.isManageProfileUser == true) {
                    $scope.$root.manageGroup = undefined;
                    $("#manage-profile-user-or-group").toggle("right");
                }

                $scope.$root.isOpenContactList = false;
                $scope.$root.isManageProfileUser = false;
                $scope.$root.isOpenGroupList = true;
            }
        };

        $scope.manageProfileGroup = function (groupId) {
            $scope.$root.isManageProfileGroup = true;

            if ($scope.$root.manageGroup == undefined)
                $("#manage-profile-user-or-group").toggle("right");

            if ($scope.$root.manageGroup == undefined || $scope.$root.manageGroup.id != groupId) {

                $scope.$root.manageGroup = $scope.profile.groups.find(x => x.id == groupId);

                // Get all members from a group.

                $.ajax({
                    type: "GET",
                    url: `${window.location.origin}/api/relationship/group/${groupId}`,
                    responseType: "application/json",
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function (response) {
                        $scope.$root.members = response;
                        $scope.$root.$digest();

                        setTimeout(() => {
                            $scope.$root.isLoadedMembers = true;
                            $scope.$root.$digest();
                        }, 1000);
                    },
                    error: function () {
                        alert("Failed to load members.");
                    }
                });
            }
        };

        $scope.addMember = function (id) {

            // Add member to group.

            $.ajax({
                type: "PATCH",
                url: `${window.location.origin}/api/relationship/group/${$scope.$root.manageGroup.id}`,
                headers: {
                    "Authorization": "Bearer " + token
                },
                contentType: "application/json",
                cache: false,
                data: JSON.stringify({ user_id: id }),
                success: function () {
                    $scope.$root.members.push($scope.$root.userListGroup.find(x => x.id == id));
                    $scope.$root.isOpenMemberList = false;
                    $scope.$root.$digest();
                },
                error: function () {
                    alert("Failed to add member to group.");
                }
            });
        };

        $scope.removeMember = function (id) {
            const confirmDeleteMember = confirm("Are you sure you want to delete this member?");

            if (confirmDeleteMember) {

                // Remove mamber from group.

                $.ajax({
                    type: "DELETE",
                    url: `${window.location.origin}/api/relationship/group/${$scope.$root.manageGroup.id}/${id}`,
                    headers: {  
                        "Authorization": "Bearer " + token
                    },
                    success: function () {
                        const index = $scope.$root.members.findIndex(x => x.id == id);

                        $scope.$root.members.splice(index, 1);
                        $scope.$root.$digest();
                    },
                    error: function () {
                        alert("Failed to remove member from group.");
                    }
                });
            }
        };

        $scope.openMemberList = function (type) {
            if (type == "group") {
                $scope.$root.isOpenMemberList = false;
                $scope.$root.$digest();
            } else {
                $scope.$root.isLoadedMembers = false;

                // Get all users.

                $.ajax({
                    type: "GET",
                    url: `${window.location.origin}/api/relationship/users`,
                    responseType: "application/json",
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function (response) {
                        $scope.$root.userListGroup = response.filter(x => x.id != $scope.profile.user.id && $scope.members.find(y => y.id == x.id) == null);
                        $scope.$root.isOpenMemberList = true;
                        $scope.$root.isLoadedMembers = true;
                        $scope.$root.$digest();
                    },
                    error: function () {
                        alert("Failed to load users.");
                    }
                });
            }
        }

        $scope.deleteGroup = function () {
            const confirmDeleteGroup = confirm("Are you sure you want to delete this group?");

            if (confirmDeleteGroup) {

                // Delete a group.

                $.ajax({
                    type: "DELETE",
                    url: `${window.location.origin}/api/relationship/group/${$scope.$root.manageGroup.id}`,
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function () {
                        const indexInProfile = $scope.profile.groups.findIndex(x => x.id == $scope.$root.manageGroup.id);
                        const indexInContacts = $scope.groups.findIndex(x => x.id == $scope.$root.manageGroup.id);

                        $scope.$root.profile.groups.splice(indexInProfile, 1);
                        $scope.$root.groups.splice(indexInContacts, 1);
                        $scope.$root.manageGroup = undefined;
                        $scope.$root.isEditingGroup = false;
                        $scope.$root.$digest();

                        $("#manage-profile-user-or-group").toggle("slide");
                    },
                    error: function () {
                        alert("Failed to delete the group.");
                    }
                });
            }
        };

        $scope.deleteUser = function () {
            const confirmDeleteGroup = confirm("Are you sure you want to delete the account?");

            if (confirmDeleteGroup) {

                // Delete user.

                $.ajax({
                    type: "DELETE",
                    url: `${window.location.origin}/api/account`,
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function () {
                        window.location.href = "/auth/register";
                    },
                    error: function () {
                        alert("Failed to delete the account.");
                    }
                });
            }
        };
    })
    .controller("ConversationController", function ($scope) {
        let userConversationId = -1;
        let groupConversationId = -1;

        // Receive messages in real time (for users). 

        connection.on("ReceiveMessageFromUser", function (fromUserId, message, dateTime) {
            const existUser = $scope.users.findIndex(x => x.id == fromUserId);

            if (existUser == -1) {

                // Get user by id.

                $.ajax({
                    type: "GET",
                    url: `${window.location.origin}/api/relationship/users/${fromUserId}`,
                    responseType: "application/json",
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function (newUser) {
                        newUser.last_message.id_user_to = $scope.profile.user.id;
                        newUser.last_message.status = 0;
                        newUser.last_message.content = message.length < 20 ? message : message.substring(0, 20) + " ...";

                        $scope.users.push(newUser);
                        $scope.$root.$digest();

                        connection.invoke("ProvideConnectedUsersAndGroups");
                    },
                    error: function () {
                        alert("Failed to get user.");
                    }
                });
            } else { 
                const user = $scope.$root.users.find(x => x.id == fromUserId);

                if (userConversationId != fromUserId) {
                    user.last_message.id_user_to = $scope.profile.user.id;
                    user.last_message.status = 0;
                } else {
                    $scope.messages.push(new Message(
                        fromUserId,
                        $scope.profile.user.id,
                        message,
                        1,
                        dateTime,
                        { name: $scope.userConversation.name, image: $scope.userConversation.image },
                        "INCOME"
                    ));
                    $scope.$digest();

                    $(".messages").animate({ scrollTop: 100000 }, "fast");

                    $.ajax({
                        type: "PUT",
                        url: `${window.location.origin}/api/message/${$scope.userConversation.id}`,
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        success: function () {
                            user.last_message.status = 1;
                            $scope.$root.$digest();

                            connection.invoke("SeenMessagesFromUser", $scope.profile.user.id, $scope.userConversation.id);
                        },
                        error: function () {
                            alert("Failed to mark as seen messages.");
                        }
                    });
                }

                user.last_message.content = message;
                $scope.$root.$digest();
            }
        });

        // Receive messages in real time (for group). 

        connection.on("ReceiveMessageFromGroup", function (fromUserId, toGroupId, message, dateTime, name, image) {
            const group = $scope.$root.groups.find(x => x.id == toGroupId);
            
            if (groupConversationId != toGroupId) {
                group.last_message.seen_members = fromUserId.toString();
                group.last_message.status = 0;
            } else {
                if ($scope.profile.user.id != fromUserId) {
                    $scope.messages.push(new Message(
                        fromUserId,
                        $scope.groupConversationId,
                        message,
                        1,
                        dateTime,
                        { name: name, image: image },
                        "INCOME"
                    ));
                    $scope.$digest();

                    $(".messages").animate({ scrollTop: 100000 }, "fast");

                    $.ajax({
                        type: "PUT",
                        url: `${window.location.origin}/api/message/group/${groupConversationId}`,
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        success: function () {
                            group.last_message.status = 1;
                            $scope.$root.$digest();
                        },
                        error: function () {
                            alert("Failed to mark as seen messages.");
                        }
                    });
                }
            }

            group.last_message.content = message;
            $scope.$root.$digest();
        });

        // Receive seen for sent messages in real time (for users). 

        connection.on("ReceiveSeenMessagesFromUser", function (userId) {
            if (userConversationId == userId) {
                for (const message of $scope.messages) {
                    if (message.status == 0)
                        message.status = 1;
                }
                $scope.$root.$digest();
            }
        });

        $scope.loadMessagesFromConversation = function () {
            if ($scope.typeConversation == "user") {
                if (userConversationId != $scope.userConversation.id) {
                    userConversationId = $scope.userConversation.id;
                    groupConversationId = -1;
                    $scope.$root.messages = [];
                    $scope.isLoadedMessages = false;

                    const user = $scope.$root.users.find(x => x.id == $scope.userConversation.id);

                    if (user == undefined) {
                        setTimeout(() => {
                            $scope.messages = [];
                            $scope.isLoadedMessages = true;
                            $scope.$digest();
                        }, 1000);
                    } else {

                        // Get all messages from a contact.

                        $.ajax({
                            type: "GET",
                            url: `${window.location.origin}/api/message/${$scope.userConversation.id}`,
                            responseType: "application/json",
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            success: function (response) {
                                const messages = [];

                                for (const message of response) {
                                    messages.push(new Message(
                                        message.id_user_from,
                                        message.id_user_to,
                                        message.content,
                                        message.status,
                                        message.date_time,
                                        (message.id_user_from == $scope.profile.user.id ? $scope.profile.user : $scope.userConversation),
                                        (message.id_user_from == $scope.profile.user.id && message.id_user_to != $scope.profile.user.id) ? "OUTCOME" : "INCOME"
                                    ));
                                }

                                $scope.messages = messages;
                                $scope.countUnreadMessages = messages.filter(x => x.status == 0).length;
                                $scope.$digest();

                                setTimeout(() => {
                                    $scope.isLoadedMessages = true;
                                    $scope.$digest();

                                    $(".messages").animate({ scrollTop: 100000 }, "fast");
                                }, 1000);
                            },
                            error: function () {
                                alert("Failed to load messages from contact.");
                            }
                        });

                        // Mark as seen all messages from a conversation between two users.

                        if (user.last_message != null && user.last_message.id_user_to == $scope.profile.user.id && user.last_message.content != "" && user.last_message.status == 0) {
                            $.ajax({
                                type: "PUT",
                                url: `${window.location.origin}/api/message/${$scope.userConversation.id}`,
                                headers: {
                                    "Authorization": "Bearer " + token
                                },
                                success: function () {
                                    user.last_message.status = 1;
                                    $scope.$root.$digest();

                                    connection.invoke("SeenMessagesFromUser", $scope.profile.user.id, $scope.userConversation.id);
                                },
                                error: function () {
                                    alert("Failed to mark as seen messages.");
                                }
                            });
                        }
                    }
                }
            } else if ($scope.typeConversation == "group") {
                if (groupConversationId != $scope.groupConversation.id) {
                    groupConversationId = $scope.groupConversation.id;
                    userConversationId = -1;
                    $scope.$root.messages = [];
                    $scope.isLoadedMessages = false;

                    // Get all messages from a group.

                    $.ajax({
                        type: "GET",
                        url: `${window.location.origin}/api/message/group/${$scope.groupConversation.id}`,
                        responseType: "application/json",
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        success: function (response) {
                            const messages = [];
                            
                            for (const message of response) {
                                messages.push(new Message(
                                    message.id_user,
                                    message.id_group,
                                    message.content,
                                    message.status,
                                    message.date_time,
                                    message.user,
                                    message.id_user == $scope.profile.user.id ? "OUTCOME" : "INCOME",
                                    message.seen_members
                                ));
                            }

                            $scope.messages = messages;
                            $scope.countUnreadMessages = messages.filter(x => x.status == 0 && x.seenMembers.indexOf($scope.profile.user.id) == -1).length;
                            $scope.$digest();

                            setTimeout(() => {
                                $scope.isLoadedMessages = true;
                                $scope.$digest();

                                $(".messages").animate({ scrollTop: 100000 }, "fast");
                            }, 1000);
                        },
                        error: function () {
                            alert("Failed to load messages from group.");
                        }
                    });

                    // Mark as seen all messages from a conversation between two users.

                    const group = $scope.$root.groups.find(x => x.id == $scope.groupConversation.id);

                    if (group.last_message != null && group.last_message.content != "" && group.last_message.status == 0 && group.last_message.seen_members != null && group.last_message.seen_members.indexOf($scope.profile.user.id) == -1) {
                        $.ajax({
                            type: "PUT",
                            url: `${window.location.origin}/api/message/group/${$scope.groupConversation.id}`,
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            success: function () {
                                group.last_message.status = 1;
                                $scope.$root.$digest();
                            },
                            error: function () {
                                alert("Failed to mark as seen messages.");
                            }
                        });
                    }
                }
            }
        };

        $scope.sendMessage = function (message) {
            if (message != null && message != "") {
                if ($scope.typeConversation == "user") {

                    const existUser = $scope.users.findIndex(x => x.id == $scope.userConversation.id);

                    if (existUser == -1) {
                        $scope.messages = [];

                        // Create a new relationship between two users.

                        $.ajax({
                            type: "POST",
                            url: `${window.location.origin}/api/relationship/contact`,
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            contentType: "application/json",
                            cache: false,
                            data: JSON.stringify({ user_id: $scope.userConversation.id }),
                            success: function () {

                                // Send message to a user.

                                $.ajax({
                                    type: "POST",
                                    url: `${window.location.origin}/api/message`,
                                    headers: {
                                        "Authorization": "Bearer " + token
                                    },
                                    contentType: "application/json",
                                    cache: false,
                                    data: JSON.stringify({ user_id: $scope.userConversation.id, message: message }),
                                    success: function () {
                                        const newMessage = new Message(
                                            $scope.profile.user.id,
                                            $scope.userConversation.id,
                                            message,
                                            0,
                                            new Date().toLocaleString('en-GB').replace(', ', 'T'),
                                            $scope.profile.user,
                                            "OUTCOME"
                                        );

                                        const newUser = $scope.userList.find(x => x.id == $scope.userConversation.id);
                                        newUser.last_message.content = newMessage.content;

                                        $scope.messages.push(newMessage);
                                        $scope.$root.users.push(newUser);
                                        $scope.$digest();

                                        $(".messages").animate({ scrollTop: 100000 }, "fast");
                                        
                                        connection.invoke("ProvideConnectedUsersAndGroups");
                                        connection.invoke("SendMessageToUser", $scope.profile.user.id, $scope.userConversation.id, newMessage.content, newMessage.dateTime);
                                    },
                                    error: function () {
                                        alert("Failed to send the message.");
                                    }
                                });
                            },
                            error: function () {
                                alert("Failed to create relationship.");
                            }
                        });
                    } else {

                        // Send message to a user.

                        $.ajax({
                            type: "POST",
                            url: `${window.location.origin}/api/message`,
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            contentType: "application/json",
                            cache: false,
                            data: JSON.stringify({ user_id: $scope.userConversation.id, message: message }),
                            success: function () {
                                const newMessage = new Message(
                                    $scope.profile.user.id,
                                    $scope.userConversation.id,
                                    message,
                                    0,
                                    new Date().toLocaleString('en-GB').replace(', ', 'T'),
                                    $scope.profile.user,
                                    "OUTCOME"
                                );
                                $scope.messages.push(newMessage);
                                $scope.$root.users.find(x => x.id == $scope.userConversation.id).last_message.content = newMessage.content;
                                $scope.$digest();
                                $scope.$root.$digest();

                                $(".messages").animate({ scrollTop: 100000 }, "fast");

                                connection.invoke("SendMessageToUser", $scope.profile.user.id, $scope.userConversation.id, newMessage.content, newMessage.dateTime);
                            },
                            error: function () {
                                alert("Failed to send the message.");
                            }
                        });
                    }
                } else if ($scope.typeConversation == "group") {

                    // Send message to a group.

                    $.ajax({
                        type: "POST",
                        url: `${window.location.origin}/api/message/group`,
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        contentType: "application/json",
                        cache: false,
                        data: JSON.stringify({ group_id: $scope.groupConversation.id, message: message }),
                        success: function () {
                            const newMessage = new Message(
                                $scope.profile.user.id,
                                $scope.groupConversation.id,
                                message,
                                0,
                                new Date().toLocaleString('en-GB').replace(', ', 'T'),
                                $scope.profile.user,
                                "OUTCOME"
                            );
                            $scope.messages.push(newMessage);
                            $scope.$root.groups.find(x => x.id == $scope.groupConversation.id).last_message.content = newMessage.content;
                            $scope.$digest();
                            $scope.$root.$digest();

                            $(".messages").animate({ scrollTop: 100000 }, "fast");

                            connection.invoke("SendMessageToGroup", $scope.profile.user.id, $scope.groupConversation.id, newMessage.content, newMessage.dateTime, $scope.profile.user.name, $scope.profile.user.image);
                        },
                        error: function () {
                            alert("Failed to send the message.");
                        }
                    });
                }
            }
        };

        $scope.$on("loadMessagesFromConversation", () => $scope.loadMessagesFromConversation());
    });