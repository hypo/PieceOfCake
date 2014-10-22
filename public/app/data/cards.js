if (window.app === undefined) window.app = {};

var CardsStore = function() {
  this.cards = {
    "card_login": {
      name: "card_login",
      title: _("TITLE.LOGING"),
      content: app.cards.LoginCard,
      noTitleBar: true,
      actions: []
    },
    "card_signup": {
      name: "card_signup",
      title: _("TITLE.SIGNUP"),
      content: app.cards.SignupCard,
      actions: [
        {
          text: _("TITLE.SIGNUP.CONFIRM"),
          actions: ["signup"]
        }
      ]
    },
    "card_shipping": {
      name: "card_shipping",
      title: _("TITLE.SHIPPING"),
      content: app.cards.ShippingCard,
      actions: [
        {
          text: _("TITLE.SHIPPING.NEXT"),
          actions: ["save-data", "push-card"],
          card: "card_confirmation",
          columns: ["name", "tel", "country", "city", "area", "zipcode", "addr"]
        }
      ]
    },
    "card_frame": {
      name: "card_frame",
      title: "加購相框",
      content: app.cards.FrameCard,
      actions: [
        {
          text: "確認",
          actions: ["push-card"],
          card: "card_confirmation"
        }
      ]
    },
    "card_confirmation": {
      name: "card_confirmation",
      title: _("TITLE.CONFIRMATION"),
      content: app.cards.ConfirmationCard,
      actions: [
        {
          text: "信用卡付款",
          actions: ["create-order"]
        }
      ]
    },
    "card_credit_card": {
      name: "card_credit_card",
      title: _("TITLE.CREDITCARD"),
      content: app.cards.CreditCardCard,
      actions: [
        {
          text: _("TITLE.CREDITCARD.CONFIRM"),
          actions: ["save-data", "order-cc"],
          columns: ["cardno", "expiry", "cvv"]
        }
      ]
    },
    "card_done": {
      name: "card_done",
      title: _("TITLE.DONE"),
      content: app.cards.DoneCard,
      actions: [
        {
          text: _("TITLE.DONE.DONE"),
          actions: ["done"]
        }
      ],
      showBackButton: false
    },
    "not_found": {
      name: "not_found",
      title: _("TITLE.NOT_FOUND"),
      content: app.cards.EmptyCard,
      actions: []
    }
  };
};

CardsStore.prototype.getCard = function(card_name, data) {
  var card = this.cards.hasOwnProperty(card_name) ? this.cards[card_name] : this.cards.not_found;

  if (data !== undefined)
    card.content = card.content(data)

  return card;
}

window.app.CardsStore = new CardsStore();
