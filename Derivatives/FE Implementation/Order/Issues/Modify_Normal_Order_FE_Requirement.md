# Modify Normal Order – FE Requirement

> **Epic:** DR-FE-ORD | **Module:** Order | **Priority:** P1 | **Status:** 📋 Ready for FE

---

## Background

Traders view the order book and order history on the **Orderbook** screen. When a normal order is pending, they can access the **Modify normal order** screen to make changes.

---

## Acceptance criteria

1. **Navigation from Orderbook to Modify normal order screen**

    *   Display according to UI [https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104&t=Hkbonf9r1expHBzf-11](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104&t=Hkbonf9r1expHBzf-11)

        *   **Information displayed:** current price, change, change rate; CE, FL, RE prices; volume, basis.

        *   **2 tabs:** Bid/Ask and Chart; default tab is **Bid/Ask**.

        *   On the **Bid/Ask** tab, show the bid/ask for the **top 3** transactions.

        *   When the user selects the **Chart** tab, replace the Bid/Ask area with the chart according to [https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11)

    *   When the user clicks the **Edit** button on a normal order in the Orderbook, the app navigates to the **Modify normal order** screen and passes all necessary information for the order to be modified.

2. **Display original order information & input new order information**

    *   The "original order" section displays:

        *   **Current order type** (Buy or Sell)

        *   **Original order price**

        *   **Quantity** (orderQuantity)

        *   **Order type** (Normal order)

        *   Data fetched from API GET /api/v1/derivatives/order/history **(TBD)**

3. **Validation fields**

    *   **Price**

        *   Must have a value; if the user leaves it blank or null, show an error toast message **Giá đặt ngoài khung cho phép**.

        *   Price must be within the **upper-lower** limits of the symbol.

            *   If outside this range, display an error toast message **Giá đặt ngoài khung cho phép**.

    *   **Quantity**

        *   Cannot be less than 0 | cannot be null.

        *   Maximum quantity allowed = result from API GET /api/v1/derivatives/order/checkAvailability.

            *   Buy/sell type passed to the API according to the original order.

            *   If the user enters an amount exceeding the available quantity, show an error toast message **Vượt quá sức mua khả dụng**.

    *   **Max quantity:** Display maximum quantity immediately after successfully calling checkAvailability.

4. **Place order**

    *   When the user clicks the **Cancel** button → return to the Trade screen, without calling any modify API.

    *   **Modify** button

        *   Only enabled when there is input for Price | Quantity from the user.

        *   When the user clicks **Modify** → Call POST /api/v1/derivatives/order/modify.

            *   On successful order placement: show a successful snackbar **Modify order for {symbol} has been executed successfully**.

            *   If there is an error: display the message from the API.
