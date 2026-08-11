import { Link } from "react-router-dom";
import { Mail, MapPin, Phone } from "lucide-react";
import styles from "./Footer.module.css";

export default function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.brand}>
          <h3>Sushi Bas Shop</h3>
          <p>Fresh sushi delivered to your door</p>
        </div>
        <div className={styles.links}>
          <h4>Quick Links</h4>
          <Link to="/">Home</Link>
          <Link to="/menu">Menu</Link>
          <Link to="/cart">Cart</Link>
        </div>
        <div className={styles.contact}>
          <h4>Contact</h4>
          <span>
            <Phone size={14} /> +380 96 179 4151
          </span>
          <span>
            <Mail size={14} /> sushi.bas.shop@gmail.com
          </span>
          <span>
            <MapPin size={14} /> Lviv, Ukraine
          </span>
        </div>
      </div>
      <div className={styles.bottom}>
        <span>
          Developed by{" "}
          <a
            href="https://www.linkedin.com/in/anton-bas-244465169/"
            target="_blank"
            rel="noopener noreferrer"
          >
            Anton Bas
          </a>
        </span>
        <span>© {currentYear} Sushi Shop</span>
      </div>
    </footer>
  );
}
