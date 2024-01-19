firebase.initializeApp({
    apiKey: "AIzaSyBzdYHBzOtXU8ufVRY74_XKJUhZgy5Px_4",
    authDomain: "the-fisherman-68f32.firebaseapp.com",
    databaseURL: "https://the-fisherman-68f32-default-rtdb.firebaseio.com",
    projectId: "the-fisherman-68f32",
    storageBucket: "the-fisherman-68f32.appspot.com",
    messagingSenderId: "152465678914",
    appId: "1:152465678914:web:985bb2c1d664bb929883db"
});

const DB = firebase.database();
const AU = firebase.auth();
const IMAGES = {
	node: $("body section#force-load-resources"),
	"10-clubs": $("body section#force-load-resources img.10-clubs"),
	"10-diamonds": $("body section#force-load-resources img.10-diamonds"),
	"10-hearts": $("body section#force-load-resources img.10-hearts"),
	"10-spades": $("body section#force-load-resources img.10-spades"),
	"11-clubs": $("body section#force-load-resources img.11-clubs"),
	"11-diamonds": $("body section#force-load-resources img.11-diamonds"),
	"11-hearts": $("body section#force-load-resources img.11-hearts"),
	"11-spades": $("body section#force-load-resources img.11-spades"),
	"12-clubs": $("body section#force-load-resources img.12-clubs"),
	"12-diamonds": $("body section#force-load-resources img.12-diamonds"),
	"12-hearts": $("body section#force-load-resources img.12-hearts"),
	"12-spades": $("body section#force-load-resources img.12-spades"),
	"13-clubs": $("body section#force-load-resources img.13-clubs"),
	"13-diamonds": $("body section#force-load-resources img.13-diamonds"),
	"13-hearts": $("body section#force-load-resources img.13-hearts"),
	"13-spades": $("body section#force-load-resources img.13-spades"),
	"1-clubs": $("body section#force-load-resources img.1-clubs"),
	"1-diamonds": $("body section#force-load-resources img.1-diamonds"),
	"1-hearts": $("body section#force-load-resources img.1-hearts"),
	"1-spades": $("body section#force-load-resources img.1-spades"),
	"2-clubs": $("body section#force-load-resources img.2-clubs"),
	"2-diamonds": $("body section#force-load-resources img.2-diamonds"),
	"2-hearts": $("body section#force-load-resources img.2-hearts"),
	"2-spades": $("body section#force-load-resources img.2-spades"),
	"3-clubs": $("body section#force-load-resources img.3-clubs"),
	"3-diamonds": $("body section#force-load-resources img.3-diamonds"),
	"3-hearts": $("body section#force-load-resources img.3-hearts"),
	"3-spades": $("body section#force-load-resources img.3-spades"),
	"4-clubs": $("body section#force-load-resources img.4-clubs"),
	"4-diamonds": $("body section#force-load-resources img.4-diamonds"),
	"4-hearts": $("body section#force-load-resources img.4-hearts"),
	"4-spades": $("body section#force-load-resources img.4-spades"),
	"5-clubs": $("body section#force-load-resources img.5-clubs"),
	"5-diamonds": $("body section#force-load-resources img.5-diamonds"),
	"5-hearts": $("body section#force-load-resources img.5-hearts"),
	"5-spades": $("body section#force-load-resources img.5-spades"),
	"6-clubs": $("body section#force-load-resources img.6-clubs"),
	"6-diamonds": $("body section#force-load-resources img.6-diamonds"),
	"6-hearts": $("body section#force-load-resources img.6-hearts"),
	"6-spades": $("body section#force-load-resources img.6-spades"),
	"7-clubs": $("body section#force-load-resources img.7-clubs"),
	"7-diamonds": $("body section#force-load-resources img.7-diamonds"),
	"7-hearts": $("body section#force-load-resources img.7-hearts"),
	"7-spades": $("body section#force-load-resources img.7-spades"),
	"8-clubs": $("body section#force-load-resources img.8-clubs"),
	"8-diamonds": $("body section#force-load-resources img.8-diamonds"),
	"8-hearts": $("body section#force-load-resources img.8-hearts"),
	"8-spades": $("body section#force-load-resources img.8-spades"),
	"9-clubs": $("body section#force-load-resources img.9-clubs"),
	"9-diamonds": $("body section#force-load-resources img.9-diamonds"),
	"9-hearts": $("body section#force-load-resources img.9-hearts"),
	"9-spades": $("body section#force-load-resources img.9-spades"),
    "back": $("body section#force-load-resources img.back"),
    "x-icon": $("body section#force-load-resources img.x-icon"),
    "k-icon": $("body section#force-load-resources img.k-icon"),
    get(id) {
        return this[id].clone();
    }
};
IMAGES.node.remove();
const USER = {
    id: "",
    name: "",
    exp: 0,
    level: 1,
    stats: {
        [`games-finished`]: 0,
        [`games-played`]: 0,
        [`times-lost`]: 0,
        [`times-won`]: 0
    },
    nodes: {
        profile: {
            board: $("<section id=\"board\"></section>"),
            username: $("<span>username</span>"),
            level: $("<span>level</span>"),
            bar: $("<div></div>"),
            exp: $("<span>exp</span>"),
            stats: $("<div class=\"stats\"></div>")
        },
        room: {
            box: $("<section id=\"room\"></section>"),
            info: $("<h1><span>Code: CODE</span><span>Password: PASS</span></h1>"),
            message: $("<p>Waiting for server messages targeted at this room...</p>")
        }
    },
    room: {
        code: "",
        password: ""
    },
    joinRoom(code, pass) {
        this.room.code = code;
        this.room.password = pass;
        this.nodes.profile.board.remove();

        $("body").append(
            this.nodes.room.box.append(
                this.nodes.room.message,
                this.nodes.room.info
            )
        );

        const THIS = this;
        const roomRef = DB.ref("/rooms/" + code);
        roomRef.on('value', snap => {
            const val = snap.val();
            if (val == null) return;

            THIS.nodes.room.message.text(val.message);
            THIS.nodes.room.info.empty();
            THIS.nodes.room.info.append(
                $(`<span>Code: ${THIS.room.code}</span>`)
            );

            if (val.locked)
                THIS.nodes.room.info.append(
                    $(`<span>Pass: ${THIS.room.password}</span>`)
                );
            
            if (val.started && val.game != null) {
                roomRef.off();

                GAME.id = val.game;
                GAME.updateRealTime();
            }
        })
    },
    updateRealTime() {
        const THIS = this;
        DB.ref("/users/" + this.id).on('value', snap => {
            const val = snap.val();
            if (val == null) return;

            THIS.exp = val.exp;
            THIS.level = val.level;
            THIS.stats = val.stats;

            THIS.nodes.profile.username.text(THIS.name);
            THIS.nodes.profile.level.text(THIS.level);
            THIS.nodes.profile.bar.css("width", `${THIS.exp%1000/10}%`);
            THIS.nodes.profile.exp.text(THIS.exp%1000);
            
            const stats = val.stats;
            THIS.nodes.profile.stats.empty();
            for (const prop in stats)
                THIS.nodes.profile.stats.append(
                    $("<div></div>").append(
                        $(`<span>${
                            [...`${prop}`.matchAll(/-?(\w+)-?/g)]
                                .map(group => group[1])
                                .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                                .join(' ')
                        }</span>`),
                        $(`<span>${stats[prop]}</span>`)
                    )
                );
        });
    },
    request(type, origin) {
        origin = origin || "client";

        const THIS = this;
        return {
            uid: THIS.id,
            "type": type,
            "origin": origin,
            content: {},
            write(key, val) {
                this.content[key] = val;
                return this;
            },
            async send(onSuccess, onFail) {
                onSuccess = onSuccess || ((response) => {});
                onFail = onFail || ((response) => {});

                const clientRef = DB.ref(`/${this.origin}/${this.uid}`);
                const requestId = await clientRef.push().key;
                const handleErr = error => { if (error != null) console.log(error) };

                clientRef.child(requestId).set({
                    uid: this.uid,
                    id: requestId,
                    type: this.type.toUpperCase(),
                    timestamp: new Date().getTime(),
                    origin: this.origin,
                    content: this.content
                }, handleErr);

                if (this.type.toUpperCase() == "USER-GOT-RESPONSE") return;

                const responsePath = [...this.origin.split('/'), this.uid];
                responsePath[responsePath.length-2] = "server";

                const responseRef = DB.ref(responsePath.join('/')); 
                responseRef.child(requestId).on('value', snap => {
                    const val = snap.val();
                    if (val == null) return;
                    responseRef.off();

                    THIS.request("USER-GOT-RESPONSE")
                        .write("prevResId", requestId)
                        .send();

                    if (val.content.success) onSuccess(val);
                    else onFail(val);
                });
            }
        }
    }
}
const GAME = {
    id: "",
    moving: "",
    message: {
        updatable: $(`<p>Wait for your turn.</p>`),
        node: $(`<section id="game-message-ui">`),
        update(msg) {
            return this.updatable.text(msg);
        }
    },
    pile: {
        node: $(`<section id="game-pile-ui" data-selected-pile="">`),
        deck: $(`<div class="cards">`),
        discard: $(`<div class="cards">`),
        updateDeck(arr) {
            this.deck.empty();

            const blank = IMAGES.get("back");

            const newImages = [];
            const THIS = this;
            for (const card of arr)
                newImages.push(blank.clone().click(() => {
                    THIS.node.attr("data-selected-pile", "deck");
                }));

            
            blank.css("visibility", "hidden");
            blank.click(() => {
                THIS.node.attr("data-selected-pile", "");
            });

            const missing = 2-newImages.length;
            for (let i=0; i<missing; i++)
                newImages.push(blank.clone());

            newImages.reverse();
            this.deck.append(...newImages);
        },
        get selectedPile() {
            return this.node.attr("data-selected-pile");
        },
        resetSelect() {
            this.node.attr("data-selected-pile", "");
        },
        updateDiscard(arr) {
            this.discard.empty();

            const newImages = [];
            const THIS = this;
            for (const card of arr)
                newImages.push(IMAGES.get(`${card.rank}-${card.suit}`).click(() => {
                    THIS.node.attr("data-selected-pile", "discard");
                }));

            const blank = IMAGES.get("back");
            blank.css("visibility", "hidden");

            const missing = 2-newImages.length;
            for (let i=0; i<missing; i++)
                newImages.push(blank.clone().click(() => {
                    THIS.node.attr("data-selected-pile", "");
                }));

            newImages.reverse();
            this.discard.append(...newImages);

            this.resetSelect();
        }
    },
    hand: {
        node: $(`<section id="game-hand-ui" data-selected-card="">`),
        get selectedCard() {
            const data = this.node.attr("data-selected-card").split('-');
            if (data.length < 2) return null;
            else return {
                rank: data[0],
                suit: data[1]
            };
        },
        resetSelect() {
            this.node.attr("data-selected-card", null);
        },
        update(arr) {
            const children = [$(`<p>You're eliminated.</p>`)];
            const THIS = this;
            for (const card of arr) {
                const rank = card.rank;
                const suit = card.suit;
                const cName = `${rank}-${suit}`;
                children.push(
                    IMAGES.get(cName).click(() => {
                        THIS.node.attr(`data-selected-card`, cName);
                    })
                );
            }

            this.node.empty();
            this.node.append(...children);
        }
    },
    option: {
        node: $(`<section id="game-option-ui">`),
        message: $(`<div class="message">SERVER MESSAGE</div>`),
        buttons: {
            node: $(`<div class="options">`),
            draw: $(`<button>Draw</button>`),
            discard: $(`<button>Discard</button>`),
            knock: $(`<button>Knock</button>`),
            disableAll() {
                this.draw.prop("disabled", true);
                this.draw.off();
                this.discard.prop("disabled", true);
                this.discard.off();
                this.knock.prop("disabled", true);
                this.discard.off();
            }
        }
    },
    players: {
        node: $(`<section id="game-players-ui">`),
        container: $(`<div class="container">`),
        update(obj) {
            this.container.empty();

            const players = [...Object.values(obj.players)];
            const playerNodes = [];
            for (const player of players) {
                const xIcons = [];
                for (let i=0; i<3; i++)
                    xIcons.push(IMAGES.get("x-icon"));
                for (let i=0; i<player.strikes; i++)
                    xIcons[i].attr("class", "striked");
                
                playerNodes.push(
                    $(`<div class="player">`).append(
                        $(`<span>${player.name}</span>`),
                        $(`<span>`).append(...xIcons),
                        $(`<span>${obj.statsHidden && player.id != USER.id ? "?" : player.value}</span>`)
                    )
                );
            }

            this.container.append(...playerNodes);
        }
    },
    order: {
        node: $(`<section id="game-order-ui">`),
        turn: $(`<h1>Turn:before #</h1>`),
        container: $(`<div class="container">`),
        update(obj) {
            this.container.empty();

            this.turn.text(obj.turn);

            const players = obj.players;
            const turnOrder = obj.turnOrder;
            const playerNodes = [];
            for (const playerName of turnOrder) {
                const knockedIcon = IMAGES.get(`k-icon`);
                if (players[playerName].knocked)
                    knockedIcon.attr("class", "knocked");
                
                playerNodes.push(
                    $(`<div class="player">`).append(
                        knockedIcon,
                        $(`<span>${playerName}</span>`)
                    )
                );
            }

            this.container.append(...playerNodes);
        }
    },
    updateRealTime() {
        $("body").empty();
        $("body").append(
            gamePileUi(),
            gamePlayersUi(),
            gameOrderUi(),
            gameHandUi(),
            gameOptionUi(),
            gameMessageUi()
        );

        const THIS = this;
        THIS.option.buttons.disableAll();
        DB.ref("/games/" + this.id).on('value', snap => {
            const val = snap.val();
            if (val == null)
                window.location.reload();

            THIS.pile.updateDeck(val.deck != null ? val.deck : []);
            THIS.pile.updateDiscard(val.discard != null ? val.discard : []);

            THIS.players.update(val);

            THIS.order.update(val);

            THIS.hand.update(
                val.players != null &&
                val.players[USER.name] != null &&
                val.players[USER.name].hand != null ?
                val.players[USER.name].hand :
                []
            );

            THIS.option.buttons.disableAll();
            if (!val.over) {
                if (val.turnOrder.includes(USER.name)) {
                    THIS.moving = val.turnOrder[0];
                    if (val.roundOver)
                        THIS.option.message.text("Waiting for tally...");
                    else if (THIS.moving == USER.name) {
                        if (val.players[USER.name].hand.length < 4) {
                            THIS.option.message.text("Would you like to draw a card or knock?");
                            THIS.option.buttons.draw.prop("disabled", false);
                            THIS.option.buttons.draw.on("click", () => {
                                THIS.option.buttons.knock.prop("disabled", true);
                                THIS.option.buttons.draw.prop("disabled", true);
                                THIS.option.message.text("Select the pile you want to draw from.");
                                THIS.pile.resetSelect();

                                $("body").on("click", function handleBodyClick() {
                                    const pile = THIS.pile.selectedPile;
                                    if (!["deck", "discard"].includes(pile)) return;

                                    USER.request("DRAW-" + pile.toUpperCase(), `gamecomms/${THIS.id}/client`)
                                        .send(goodResponse => {
                                            $("body").off("click", handleBodyClick);
                                            THIS.option.buttons.disableAll();
                                        }, badResponse => {
                                            THIS.option.message.text(badResponse.content.message);
                                            THIS.option.buttons.draw.prop("disabled", false);
                                            THIS.option.buttons.knock.prop("disabled", false);
                                        });
                                });
                            });

                            // check if you can also knock
                            let canKnock = true;
                            for (const pName of val.turnOrder)
                                if (val.players[pName].knocked) {
                                    canKnock = false;
                                    break;
                                }
                            
                            if (canKnock) {
                                THIS.option.buttons.knock.prop("disabled", false);
                                THIS.option.buttons.knock.on("click", () => {
                                    THIS.option.buttons.knock.prop("disabled", true);
                                    THIS.option.buttons.draw.prop("disabled", true);
                                    USER.request("KNOCK", `gamecomms/${THIS.id}/client`)
                                        .send(goodResponse => {
                                            THIS.option.buttons.disableAll();
                                        }, badResponse => {
                                            THIS.option.message.text(badResponse.content.message);
                                            THIS.option.buttons.draw.prop("disabled", false);
                                            THIS.option.buttons.knock.prop("disabled", false);
                                        });
                                });
                            }
                        } else {
                            // have 4 cards
                            THIS.option.message.text(`Select a card from your hand to discard.`);

                            let discardSetup = false;

                            THIS.hand.resetSelect();
                            $("body").on("click", function handleBodyClick() {
                                const choice = THIS.hand.selectedCard;
                                if (choice == null) return;

                                THIS.option.message.text(`You have selected the ${choice.rank} of ${choice.suit}. Click on Discard to confirm.`);

                                if (!discardSetup) {
                                    discardSetup = true;
                                    THIS.option.buttons.discard.prop("disabled", false);
                                    THIS.option.buttons.discard.on("click", () => {
                                        THIS.option.buttons.discard.prop("disabled", true);
                                        const choice2 = THIS.hand.selectedCard;

                                        USER.request("DISCARD", `gamecomms/${THIS.id}/client`)
                                            .write("rank", choice2.rank)
                                            .write("suit", choice2.suit)
                                            .send(goodResponse => {
                                                $("body").off("click", handleBodyClick);
                                                THIS.option.buttons.disableAll();
                                            }, badResponse => {
                                                THIS.option.message.text(badResponse.content.message);
                                                THIS.option.buttons.discard.prop("disabled", false);
                                            });
                                    });
                                }
                            });
                        }
                    } else
                        THIS.option.message.text("Waiting for your turn...");
                } else
                    THIS.option.message.text("You got 3 strikes. You are out.");
            } else 
                THIS.option.message.text("Game is over.");
            

            THIS.message.update(val.message != null ? val.message : "NO SERVER MESSAGE");
        });
    }
}

let FIRST_TIME = true;
AU.onAuthStateChanged(user => {
    const noUser = user == null;
    if (!FIRST_TIME) {
        if (noUser) window.location.reload();
        return;
    }
    FIRST_TIME = false;

    if (noUser) noAuth();
    else hasAuth(user);
});

function noAuth() {
    console.log("No auth detected.");
    
    const usernameInput = $("<input type=\"text\" placeholder=\"Enter Username\" required>");
    const passwordInput = $("<input type=\"password\" placeholder=\"Enter Password\" required>");

    const values = () => [usernameInput.val(), passwordInput.val()];

    const messageOut = $("<p>Authentication required to access content.</p>");
    const registerBtn = $("<button>Register</button>").click(async () => {
        setDisable(true);
        messageOut.text("Registering...");

        const username = values()[0];
        if (username.length < 6 || username.length > 10) {
            messageOut.text("Username needs to be between 6 and 10 characters.");
            setDisable(false);
            return;
        }
        AU.createUserWithEmailAndPassword(`${username}@thirtyone.bths.edu`, values()[1])
            .then(cred => cred.user)
            .then(async user => {
                messageOut.text("Setting up game account...");

                USER.id = `${user.uid}`;
                USER.name = `${username}`;
                USER.request("MAKE-ACCOUNT")
                    .write("name", username)
                    .send(goodResponse => {
                        messageOut.text("Account successfully made.");
                        window.location.reload();
                    }, badResponse => {
                        messageOut.text(badResponse.content.message);
                        alert("Unknown error appeared. Automatically logged you out.")
                        AU.signOut();
                        setDisable(false);
                    });
            }).catch(error => {
                switch (error.code) {
                case "auth/argument-error":
                    messageOut.text("Not all fields are filled out correctly.");
                    break;
                case "auth/email-already-in-use":
                    messageOut.text("Username already used by someone else.");
                    break;
                case "auth/invalid-email":
                    messageOut.text("Username not accepted.")
                    break;
                case "auth/weak-password":
                    messageOut.text("Password is too weak.");
                    break;
                default:
                    messageOut.text(error.message);
                }
                setDisable(false);
            });
    });
    const loginBtn = $("<button>Log In</button>").click(async () => {
        setDisable(true);
        messageOut.text("Logging in...");

        const username = values()[0];
        if (username.length < 6) {
            messageOut.text("Invalid username. Not accepted.");
            setDisable(false);
            return;
        }

        AU.signInWithEmailAndPassword(`${username}@thirtyone.bths.edu`, values()[1])
            .then(cred => cred.user)
            .then(async user => {
                messageOut.text("Successfully signed in.");
                window.location.reload();
            }).catch(error => {
                switch (error.code) {
                case "auth/argument-error":
                    messageOut.text("Not all fields are filled out correctly.");
                    break;
                case "auth/invalid-email":
                    messageOut.text("Invalid username. Not accepted.");
                    break;
                case "auth/user-disabled":
                    messageOut.text("Account is disabled.")
                    break;
                case "auth/user-not-found":
                    messageOut.text("No such account exists.");
                    break;
                case "auth/wrong-password":
                    messageOut.text("Incorrect password.");
                    break;
                default:
                    messageOut.text(error.message);
                }
                setDisable(false);
            });
    });
    function setDisable(state) {
        registerBtn.prop("disabled", state);
        loginBtn.prop("disabled", state);
    }

    $("body").append(
        $("<section id=\"auth\"></section>").append(
            $("<form spellcheck=\"false\"></form>").append(
                usernameInput,
                passwordInput
            ),
            messageOut,
            $("<div></div>").append(
                registerBtn,
                loginBtn
            )
        )
    );
}

function hasAuth(user) {
    console.log("Auth detected.");
    
    USER.id = `${user.uid}`;
    USER.name = `${user.email.split('@')[0]}`;
    USER.updateRealTime();

    $("body").append(
        USER.nodes.profile.board.append(
            $("<div class=\"profile\"></div>").append(
                $("<h1></h1>").append(
                    USER.nodes.profile.username,
                    USER.nodes.profile.level
                ),
                $("<div class=\"exp\"><span>space</span></div>").append(
                    USER.nodes.profile.bar,
                    USER.nodes.profile.exp
                ),
                USER.nodes.profile.stats,
                $("<div class=\"auth\"></div>").append(
                    $("<button>Sign Out</button>").click(() => {
                        AU.signOut();
                    })
                )
            ),
            queueUi()
        )
    );
}

function queueUi() {
    const box = $("<div class=\"queue\" data-stage=\"default\"></div>");
    const title = $("<h1>Play a Game!</h1>");
    box.append(
        title,
        $("<div data-stage=\"default\">").append(
            $("<button>Make a Room</button>").click(() => {
                box.attr("data-stage", "make");
                title.text("Make a Room!");
            }),
            $("<button>Join a Room</button>").click(() => {
                box.attr("data-stage", "join");
                title.text("Join a Room!");
            })
        )
    );

    const makeInput = $("<input type=\"password\" placeholder=\"Add Password (Optional)\"required>");
    const makeMsg = $("<p>Type in a password for the room if you want.</p>");
    const makeBackBtn = $("<button>Back</button>").click(() => {
            box.attr("data-stage", "default");
            title.text("Play a Game!");
            makeInput.val(null);
        });
    const makeBtn = $("<button>Make</button>").click(() => {
            setMakeDisable(true);
            makeMsg.text("Making room...");
            const pass = makeInput.val();
            
            const request = USER.request("MAKE-ROOM");
            if (pass.length > 0)
                request.write("password", pass);
            
            request.send(goodResponse => {
                makeMsg.text("Room successfully made.");
                USER.joinRoom(goodResponse.content.code, pass);
            }, badResponse => {
                makeMsg.text(badResponse.content.message);
                setMakeDisable(false);
            });
        });
    function setMakeDisable(state) {
        makeBackBtn.prop("disabled", state);
        makeBtn.prop("disabled", state);
    }
    box.append(
        $("<div data-stage=\"make\" spellcheck=\"false\">").append(
            makeInput,
            makeMsg,
            $("<section></section>").append(
                makeBackBtn,
                makeBtn
            )
        )
    );

    const joinCodeInput = $("<input type=\"text\" placeholder=\"Enter Room Code\"required>");
    const joinPassInput = $("<input type=\"password\" placeholder=\"Enter Room Password\"required>");
    const joinMsg = $("<p>Fill in the neccessary fields to join an exisitng room.</p>");
    const joinBackBtn = $("<button>Back</button>").click(() => {
            box.attr("data-stage", "default");
            title.text("Play a Game!");
            joinCodeInput.val(null);
            joinPassInput.val(null);
        });
    const joinBtn = $("<button>Join</button>").click(() => {
            setJoinDisable(true);
            joinMsg.text("Joining room...");
            const code = joinCodeInput.val();
            const pass = joinPassInput.val();

            const request = USER.request("JOIN-ROOM");
            if (code.length > 0)
                request.write("code", code);
            if (pass.length > 0)
                request.write("password", pass);
            request.send(goodResponse => {
                joinMsg.text("Successfully joined room.");
                USER.joinRoom(code, pass);
            }, badResponse => {
                joinMsg.text(badResponse.content.message);
                setJoinDisable(false);
            });
        });
    function setJoinDisable(state) {
        joinBackBtn.prop("disabled", state);
        joinBtn.prop("disabled", state);
    }
    box.append(
        $("<div data-stage=\"join\" spellcheck=\"false\">").append(
            joinCodeInput,
            joinPassInput,
            joinMsg,
            $("<section></section>").append(
                joinBackBtn,
                joinBtn
            )
        )
    );
                        
    return box;
}

function gameMessageUi() {
    const box = GAME.message.node;
    box.append(GAME.message.updatable);
    return box;
}

function gamePileUi() {
    const box = GAME.pile.node;
    box.append(
        $(`<div class="pile">`).append(
            GAME.pile.deck,
            $(`<h1>Deck</h1>`)
        ),
        $(`<div class="pile">`).append(
            GAME.pile.discard,
            $(`<h1>Discard</h1>`)
        )
    );
    return box;
}

function gameHandUi() {
    const box = GAME.hand.node;
    return box;
}

function gameOptionUi() {
    const box = GAME.option.node;
    box.append(
        GAME.option.message,
        GAME.option.buttons.node.append(
            GAME.option.buttons.draw,
            GAME.option.buttons.discard,
            GAME.option.buttons.knock
        )
    );
    return box;
}

function gamePlayersUi() {
    const box = GAME.players.node;
    box.append(
        $(`<h1>Players</h1>`),
        GAME.players.container
    );
    return box;
}

function gameOrderUi() {
    const box = GAME.order.node;
    box.append(
        GAME.order.turn,
        GAME.order.container
    );

    return box;
}

