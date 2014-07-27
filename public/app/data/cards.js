if (window.app === undefined) window.app = {};

var CardsStore = function() {
  this.cards = {
    "card_login": {
      name: "card_login",
      title: "登入",
      content: app.cards.LoginCard,
      noTitleBar: true,
      actions: []
    },
    "card_signup": {
      name: "card_signup",
      title: "註冊",
      content: app.cards.SignupCard,
      actions: [
        {
          text: "確認",
          actions: ["signup"]
        }
      ]
    },
    "card_shipping": {
      name: "card_shipping",
      title: "寄件資訊",
      content: app.cards.ShippingCard,
      actions: [
        {
          text: "下一步",
          actions: ["save-data", "push-card"],
          card: "card_frame",
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
      title: "訂單明細",
      content: app.cards.ConfirmationCard,
      actions: [
        {
          text: "信用卡付款",
          actions: ["push-data", "push-card"],
          changes: [["type", "credit-card"]],
          card: "card_credit_card"
        }
      ]
    },
    "card_credit_card": {
      name: "card_credit_card",
      title: "信用卡付款",
      content: app.cards.CreditCardCard,
      actions: [
        {
          text: "確認",
          actions: ["save-data", "create-order"],
          columns: ["cardno", "expiry", "cvv"]
        }
      ]
    },
    "card_done": {
      name: "card_done",
      title: "訂單成立",
      content: app.cards.DoneCard,
      actions: [
        {
          text: "完成",
          actions: ["done"]
        }
      ],
      showBackButton: false
    },
    "not_found": {
      name: "not_found",
      title: "找不到卡片",
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
