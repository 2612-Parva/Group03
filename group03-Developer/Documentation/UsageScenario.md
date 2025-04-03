# Usage Scenarios

### User Authentication

#### Registration and Email Verification
1. Navigate to the sign-up page
2. Enter your Dalhousie email address, Banner ID, and password
3. Click "Register"
4. Check your email for the verification OTP (One-Time Password)
5. Enter the OTP on the verification page to activate your account

![Sign Up Page](./assets/signup.png)

#### Login
1. Navigate to the login page
2. Enter your email and password
3. Click "Login"
4. You will be redirected to the marketplace homepage

![Login Page](./assets/loginpage.png)


#### Password Recovery
1. Click "Forgot Password" on the login page
2. Enter your registered email address
3. Check your email for a password reset link
4. Click the link and set a new password

![Login Page](./assets/PassworReset.png)

### Listing Management

#### Creating a New Listing
1. Click "Create Listing" in the navigation menu
2. Fill in the listing details:
   - Title and description
   - Category
   - Price
   - Quantity available
   - Toggle "Allow Bidding" if you want to accept bids
   - Set a starting bid price (if bidding is enabled)
3. Upload images (up to 5 images)
4. Click "Create Listing"

![Selling Page](./assets/SellingPage.png)

#### Searching for Listings
1. Use the search bar at the top of the marketplace
2. Enter keywords related to the item you're looking for
3. Filter results by category, price range, or listing type
4. Sort results by relevance, price, or date

![Search For Listings](./assets/searchfunction.png)


### Shopping Features

#### Adding Items to Cart
1. Browse listings and find an item you want to purchase
2. Click "Add to Cart"
3. Specify the quantity (if applicable)
4. The item will be added to your shopping cart

![Add To Cart](./assets/AddToCart.png)

#### Managing Your Cart
1. Click the cart icon to view your shopping cart
2. Update quantities or remove items as needed
3. Proceed to checkout when ready

![Manage Cart](./assets/managecart.png)


#### Checkout Process
1. Review the items in your cart
2. Click "Proceed to Checkout"
3. Verify your billing and shipping information

![Checkout](./assets/checkout.png)


#### Payment Process
1. After Checkout Process
1. Click "Continue to Payment"
2. Enter your payment details on the secure Stripe checkout page
   a. You can use this test card from "4242424242424242", random number on the CVC and a future date for the expiration date.
3. Once payment is complete, you'll be redirected to an order confirmation page

![Payment](./assets/StripeLink.png)


#### Wishlist Management
1. Click the heart icon on any listing to add it to your wishlist
2. View your wishlist by clicking "Wishlist" in the user menu
3. Move items from your wishlist to your cart or remove them

![Checkout](./assets/Wishlist.png)



### Bidding System

#### Placing a Bid
1. Find a listing that allows bidding 
2. Click "Place Bid"
3. Enter your bid amount (must be above the starting bid)
4. Optionally add additional terms or notes
5. Submit your bid

![Place a Bid](./assets/MakeABid.png)

#### Managing Bids as a Buyer
1. Go to "My Bids" in the user menu
2. View all your active, accepted, and rejected bids
3. For accepted bids, proceed to payment
![Place a Bid](./assets/MyBids.png)


#### Managing Bids as a Seller
1. Go to "My Listings" and select a listing with active bids
2. View all bids received for that listing
3. For each bid, you can:
   - Accept the bid (automatically rejects all other bids)
   - Reject the bid
   - Counter with a different price
4. When you accept a bid, the buyer will be notified and can proceed to payment

![Place a Bid](./assets/ManageBids.png)


#### Finalizing Auction (Auto-select highest bid)
1. Go to "My Listings" and select a listing with multiple bids
2. Click "Finalize Auction"
3. The system will automatically select the highest bid as the winner
4. The winning bidder will be notified and can proceed to payment

![Place a Bid](./assets/FinalizeBids.png)

### Messaging and Notifications

#### Sending a Message & Viewing and Responding to Messages
   #### Sending a Message
   1. Navigate to a listing
   2. Click "Chat"
   3. Type your message
   4. Click "Send"

   #### Viewing and Responding to Messages
   1. Go to "Messages" in the user menu
   2. Select a conversation from the list
   3. View the message history
   4. Type your response and send

   ![Chat](./assets/Chat.png)



#### Notifications
- Real-time notifications appear for:
  - New messages
  - Bid updates (accepted, rejected, or countered)
  - New listings that match your interests
  - Order status changes

![Notification](./assets/Notification.png)


#### Notification Preferences
1. Go to "Settings" > "Notification Preferences"
2. Toggle which types of notifications you want to receive:
   - Message notifications
   - Bid notifications
   - New listing notifications

![NotifPref](./assets/NotifPref.png)

### Reviews and Ratings

#### Writing a Review
1. Go to "My Orders" in the user menu
2. Find a completed order
3. Click "Write a Review"
4. Rate the item from 1-5 stars
5. Write your review comments
6. Submit your review
![Writing a Review ](./assets/SubReview.png)


#### Viewing Reviews
1. Navigate to any listing
2. Scroll down to the "Reviews" section
3. View all reviews left by previous buyers
4. See the overall rating for the listing
![View a Review ](./assets/ViewReview.png)


### User Preferences

#### Updating Profile Information
1. Go to "Settings" > "Edit Profile"
2. Update your profile information
3. Click "Save Changes"
![Updating Profile Information](./assets/UserPref.png)


#### Managing Notification Preferences
1. Go to "Settings" > "Notification Preferences"
2. Enable or disable different types of notifications
3. Click "Save Preferences"
![NotifPref](./assets/NotifPref.png)


#### Viewing Account Statistics
1. Navigate to "Dashboard" by clicking on the logo by the top-left.
2. View your account statistics:
   - As a seller: number of listings, items sold, average rating
   - As a buyer: items purchased, active bids, reviews given
![Dashbaord](./assets/Dashbaord.png)

