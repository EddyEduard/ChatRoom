const connection = new signalR.HubConnectionBuilder().withUrl("/chat").build();
const token = getCookie("token");

// Connect to socket.

connection.start();

// Message constructor.

function Message(imgLink, idFrom, idTo, content, status, dateTime, type) {
    this.imgLink = imgLink;
    this.idFrom = idFrom;
    this.idTo = idTo;
    this.content = content;
    this.status = status;
    this.dateTime = dateTime;
    this.type = type;
}

angular.module("ChatRoom", [])
    .controller("ProfileController", function ($scope) {
        // Get profile.

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
            },
            error: function () {
                alert("Failed to load profile.");
            }
        })
    })
    .controller("ContactController", function ($scope) {
        $scope.$root.isOpenContactList = false;

        $scope.openConversation = function (id, type) {
            if(type == "user")
                $scope.$root.userConversation = $scope.users.find(x => x.id == id);
            else if (type == "group")
                $scope.$root.groupConversation = $scope.groups.find(x => x.id == id);

            $scope.$root.typeConversation = type;
            $scope.$root.$broadcast("loadMessagesFromConversation");
        };

        $scope.addContact = function (id) {
            $scope.$root.userConversation = $scope.userList.find(x => x.id == id);

            $scope.$root.typeConversation = "user";
            $scope.$root.isOpenedConversation = true;
            $scope.$root.isOpenContactList = false;
        };

        $scope.openContactList = function (type) {
            if (type == "my") {
                $scope.$root.isOpenContactList = false;
                $scope.$root.$digest();
            } else {
                // Get all users.

                $.ajax({
                    type: "GET",
                    url: `${window.location.origin}/api/relationship/users`,
                    responseType: "application/json",
                    headers: {
                        "Authorization": "Bearer " + token
                    },
                    success: function (response) {
                        $scope.$root.userList = response.filter(x => x.id != $scope.profile.id && $scope.users.find(y => y.id == x.id) == null);
                        $scope.$root.isOpenContactList = true;
                        $scope.$root.$digest();
                    },
                    error: function () {
                        alert("Failed to load users.");
                    }
                });
            }
        }

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
                    connection.invoke("ConnectUser", $scope.profile.id);
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
            },
            error: function () {
                alert("Failed to load contacts.");
            }
        });
    })
    .controller("ConversationController", function ($scope) {
        let userConversationId = -1;
        let groupConversationId = -1;

        // Receive messages in real time (for users). 

        connection.on("ReceiveMessageFromUser", function (fromUserId, message, dateTime) {
            const existUser = $scope.users.findIndex(x => x.id == $scope.userConversation.id);
           
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
                        console.log(newUser)
                        newUser.last_message.id_user_to = $scope.profile.id;
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
                    user.last_message.id_user_to = $scope.profile.id;
                    user.last_message.status = 0;
                } else {
                    $scope.messages.push(new Message(
                        "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                        fromUserId,
                        $scope.profile.id,
                        message,
                        1,
                        dateTime,
                        "INCOME"
                    ));
                    $scope.$digest();

                    $(".messages").animate({ scrollTop: 100000 }, "fast");

                    $.ajax({
                        type: "PUT",
                        url: `${window.location.origin}/api/message/${$scope.userConversation.id}`,
                        responseType: "application/json",
                        headers: {
                            "Authorization": "Bearer " + token
                        },
                        success: function () {
                            user.last_message.status = 1;
                            $scope.$root.$digest();
                        },
                        error: function () {
                            alert("Failed to mark as seen messages.");
                        }
                    });
                }

                user.last_message.content = message.length < 20 ? message : message.substring(0, 20) + " ...";
                $scope.$root.$digest();
            }
        });

        // Receive messages in real time (for group). 

        connection.on("ReceiveMessageFromGroup", function (fromUserId, toGroupId, message, dateTime) {
            const group = $scope.$root.groups.find(x => x.id == toGroupId);

            if (groupConversationId != toGroupId) {
                group.last_message.seen_members = fromUserId.toString();
                group.last_message.status = 0;
            } else {
                if ($scope.profile.id != fromUserId) {
                    $scope.messages.push(new Message(
                        "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                        fromUserId,
                        $scope.groupConversation.id,
                        message,
                        1,
                        dateTime,
                        "INCOME"
                    ));
                    $scope.$digest();

                    $(".messages").animate({ scrollTop: 100000 }, "fast");

                    $.ajax({
                        type: "PUT",
                        url: `${window.location.origin}/api/message/group/${$scope.groupConversation.id}`,
                        responseType: "application/json",
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

            group.last_message.content = message.length < 20 ? message : message.substring(0, 20) + " ...";
            $scope.$root.$digest();
        });

        $scope.loadMessagesFromConversation = function () {
            if ($scope.typeConversation == "user") {
                if (userConversationId != $scope.userConversation.id) {
                    userConversationId = $scope.userConversation.id;
                    groupConversationId = -1;
                    $scope.isOpenedConversation = false;
                    $scope.$root.messages = [];

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
                                    "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                                    message.id_user_from,
                                    message.id_user_to,
                                    message.content,
                                    message.status,
                                    message.date_time,
                                    (message.id_user_from == $scope.profile.id && message.id_user_to != $scope.profile.id) ? "OUTCOME" : "INCOME"
                                ));
                            }

                            $scope.messages = messages;
                            $scope.isOpenedConversation = true;
                            $scope.$digest();

                            $(".messages").animate({ scrollTop: 100000 }, "fast");
                        },
                        error: function () {
                            alert("Failed to load messages from contact.");
                        }
                    });

                    // Mark as seen all messages from a conversation between two users.

                    const user = $scope.$root.users.find(x => x.id == $scope.userConversation.id);

                    if (user.last_message != null && user.last_message.content != "" && user.last_message.status == 0) {
                        $.ajax({
                            type: "PUT",
                            url: `${window.location.origin}/api/message/${$scope.userConversation.id}`,
                            responseType: "application/json",
                            headers: {
                                "Authorization": "Bearer " + token
                            },
                            success: function () {
                                user.last_message.status = 1;
                                $scope.$root.$digest();
                            },
                            error: function () {
                                alert("Failed to mark as seen messages.");
                            }
                        });
                    }
                }
            } else if ($scope.typeConversation == "group") {
                if (groupConversationId != $scope.groupConversation.id) {
                    groupConversationId = $scope.groupConversation.id;
                    userConversationId = -1;
                    $scope.isOpenedConversation = false;
                    $scope.$root.messages = [];

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
                                    "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                                    message.id_user,
                                    message.id_group,
                                    message.content,
                                    message.status,
                                    message.date_time,
                                    message.id_user == $scope.profile.id ? "OUTCOME" : "INCOME"
                                ));
                            }

                            $scope.messages = messages;
                            $scope.isOpenedConversation = true;
                            $scope.$digest();

                            $(".messages").animate({ scrollTop: 100000 }, "fast");
                        },
                        error: function () {
                            alert("Failed to load messages from group.");
                        }
                    });

                    // Mark as seen all messages from a conversation between two users.

                    const group = $scope.$root.groups.find(x => x.id == $scope.groupConversation.id);

                    if (group.last_message != null && group.last_message.content != "" && group.last_message.status == 0 && group.last_message.seen_members != null && group.last_message.seen_members.indexOf($scope.profile.id) == -1) {
                        $.ajax({
                            type: "PUT",
                            url: `${window.location.origin}/api/message/group/${$scope.groupConversation.id}`,
                            responseType: "application/json",
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
                                            "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                                            $scope.profile.id,
                                            $scope.userConversation.id,
                                            message,
                                            0,
                                            Date.now,
                                            "OUTCOME"
                                        );

                                        const newUser = $scope.userList.find(x => x.id == $scope.userConversation.id);
                                        newUser.last_message.content = newMessage.content;

                                        $scope.messages.push(newMessage);
                                        $scope.$root.users.push(newUser);
                                        $scope.$digest();

                                        $(".messages").animate({ scrollTop: 100000 }, "fast");

                                        connection.invoke("ProvideConnectedUsersAndGroups");
                                        connection.invoke("SendMessageToUser", $scope.profile.id, $scope.userConversation.id, newMessage.content, newMessage.dateTime);
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
                                    "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                                    $scope.profile.id,
                                    $scope.userConversation.id,
                                    message,
                                    0,
                                    Date.now,
                                    "OUTCOME"
                                );
                                $scope.messages.push(newMessage);
                                $scope.$root.users.find(x => x.id == $scope.userConversation.id).last_message.content = newMessage.content;
                                $scope.$digest();
                                $scope.$root.$digest();

                                $(".messages").animate({ scrollTop: 100000 }, "fast");

                                connection.invoke("SendMessageToUser", $scope.profile.id, $scope.userConversation.id, newMessage.content, newMessage.dateTime);
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
                                "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                                $scope.profile.id,
                                $scope.groupConversation.id,
                                message,
                                0,
                                Date.now,
                                "OUTCOME"
                            );
                            $scope.messages.push(newMessage);
                            $scope.$root.groups.find(x => x.id == $scope.groupConversation.id).last_message.content = newMessage.content;
                            $scope.$digest();
                            $scope.$root.$digest();

                            $(".messages").animate({ scrollTop: 100000 }, "fast");

                            connection.invoke("SendMessageToGroup", $scope.profile.id, $scope.groupConversation.id, newMessage.content, newMessage.dateTime);
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