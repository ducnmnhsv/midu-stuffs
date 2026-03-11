# Modify Stop Order – FE Requirement

> **Epic:** DR-FE-ORD | **Module:** Order | **Priority:** P1 | **Status:** 📋 Ready for FE

---

## Background

Traders view the order book and order history on the **Orderbook** screen. When a stop order is pending, they can access the **Modify stop order** screen to make changes (trigger price, order price, quantity, affected date).

---

## Acceptance criteria

1. **Navigation from Orderbook to Modify stop order screen**

    *   Display according to UI [https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104&t=Hkbonf9r1expHBzf-11](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005008-236104&t=Hkbonf9r1expHBzf-11)

        *   **Information displayed:** current price, change, change rate; CE, FL, RE prices; volume, basis.

        *   **2 tabs:** Bid/Ask and Chart; default tab is **Bid/Ask**.

        *   On the **Bid/Ask** tab, show the bid/ask for the **top 3** transactions.

        *   When the user selects the **Chart** tab, replace the Bid/Ask area with the chart according to [https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40006583-230683&t=Hkbonf9r1expHBzf-11)

    *   When the user clicks the **Edit** button on a stop order in the Orderbook, the app navigates to the **Modify stop order** screen and passes all necessary information for the order to be modified.

2. **Display original order information & input new order information**

    *   The "original order" section displays:

        *   **Current order type** (Buy or Sell)

        *   **Original order price** (and trigger price if applicable)

        *   **Quantity** (orderQuantity)

        *   **Order type** (Stop order)

        *   Data fetched from API GET /api/v1/derivatives/order/history **(TBD)**

    *   The modify form includes: **Trigger price**, **Order price**, **Quantity**, **Affected date**, **Max quantity** (read-only).

3. **Validation fields**

    *   **Trigger price**

        *   Must follow validation rules per Derivatives spec (e.g. vs current price if required by Backend).

    *   **Order price**

        *   Must have a value; if the user leaves it blank or null, show an error toast message **Giá đặt ngoài khung cho phép**.

        *   Order price must be within the **CE–RE** range of the symbol.

            *   If outside this range, display an error toast message **Giá đặt ngoài khung cho phép**.

    *   **Quantity**

        *   Cannot be less than 0 | cannot be null.

        *   Maximum quantity allowed = result from API GET /api/v1/derivatives/order/checkAvailability.

            *   Buy/sell type passed to the API according to the original order.

            *   If the user enters an amount exceeding the available quantity, show an error toast message **Vượt quá sức mua khả dụng**.

    *   **Affected date**

        *   User cannot select a date in the past; only today or future dates.

        *   Only **one day** is selectable (start date = end date).

        *   When the user taps to select a date, open the **date picker modal** per design.

    *   **Max quantity:** Display maximum quantity immediately after successfully calling checkAvailability.

4. **Place order**

    *   When the user clicks the **Cancel** button → return to the Trade screen, without calling any modify API.

    *   **Modify** button

        *   Only enabled when there is input for Trigger price | Order price | Quantity | Affected date from the user.

        *   When the user clicks **Modify** → Call POST /api/v1/derivatives/stopOrder/modify (request body per API spec).

            *   On successful order placement: show a successful snackbar **Modify order for {symbol} has been executed successfully**.

            *   If there is an error: display the message from the API.
