import styles from './Loading.module.css'

interface Props {
  text?: string
}

export default function Loading({ text = 'Loading...' }: Props) {
  return (
    <div className={styles.loading}>
      <div className={styles.spinner} />
      <p>{text}</p>
    </div>
  )
}