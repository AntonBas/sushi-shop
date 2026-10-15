import styles from "./PrivacyPolicyPage.module.css";

const CONTACT_EMAIL = "sushi.bas.shop@gmail.com";

interface PolicySection {
  title: string;
  items: string[];
}

const SECTIONS: PolicySection[] = [
  {
    title: "What we collect",
    items: [
      "Account details: name, email and phone number, plus the delivery address you save in your profile.",
      "If you sign in with Google: your email address and name from your Google profile. We do not get access to your Google password, contacts or any other Google data.",
      "Orders, payment status and reviews you post. Reviews are shown publicly with your name.",
      "Card payments are handled on Stripe's checkout page. Card details never reach our servers.",
    ],
  },
  {
    title: "How we use it",
    items: [
      "To create and secure your account and keep you signed in.",
      "To take, deliver and track your orders, including live order status updates.",
      "To send service emails: account verification, password reset, email change and order confirmations.",
    ],
  },
  {
    title: "Cookies and browser storage",
    items: [
      "We use an httpOnly authentication cookie to keep you signed in. We do not use advertising or tracking cookies.",
      "Your cart and theme preference are stored in your browser's local storage.",
    ],
  },
  {
    title: "Who processes your data",
    items: [
      "Stripe processes payments. Brevo delivers emails.",
      "The application is hosted on Render and Vercel, with data stored on Neon (PostgreSQL), Upstash (Redis) and Cloudinary (product images).",
      "We do not sell your data or share it for advertising.",
    ],
  },
  {
    title: "Your choices",
    items: [
      "You can view and update your profile and delivery address at any time.",
      `To delete your account and personal data, email ${CONTACT_EMAIL}. Order and payment records may be kept where needed for refunds.`,
    ],
  },
];

export default function PrivacyPolicyPage() {
  return (
    <div className={styles.page}>
      <h1>Privacy Policy</h1>
      <p className={styles.subtitle}>What data Sushi Bas Shop collects and how it is used</p>

      <div className={styles.sections}>
        {SECTIONS.map((section) => (
          <section key={section.title} className={styles.section}>
            <h2>{section.title}</h2>
            <ul>
              {section.items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
        ))}

        <section className={styles.section}>
          <h2>Contact</h2>
          <p>
            Questions about your data: <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>
          </p>
        </section>
      </div>
    </div>
  );
}
