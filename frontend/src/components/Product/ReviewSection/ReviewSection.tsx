import { useState, useEffect } from 'react'
import { Star } from 'lucide-react'
import { useApi } from '../../../hooks/common/useApi'
import * as reviewsApi from '../../../api/reviews'
import { useNotification } from '../../../context/NotificationContext'
import Button from '../../UI/Button/Button'
import Pagination from '../../UI/Pagination/Pagination'
import type { ReviewResponse } from '../../../types'
import styles from './ReviewSection.module.css'

interface Props {
  productId: number
}

export default function ReviewSection({ productId }: Props) {
  const { showNotification } = useNotification()
  const createApi = useApi<ReviewResponse>()
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [reviewPage, setReviewPage] = useState(0)
  const [totalReviewPages, setTotalReviewPages] = useState(0)
  const [newRating, setNewRating] = useState(5)
  const [newComment, setNewComment] = useState('')

  const loadReviews = (page: number) => {
    reviewsApi.getReviews(productId, page).then((res) => {
      setReviews(res.content)
      setTotalReviewPages(res.totalPages)
    })
  }

  useEffect(() => {
    loadReviews(0)
  }, [productId])

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    try {
      await createApi.execute(() =>
        reviewsApi.createReview({
          productId,
          rating: newRating,
          comment: newComment || undefined,
        })
      )
      setNewComment('')
      setNewRating(5)
      showNotification('Review submitted', 'success')
      loadReviews(reviewPage)
    } catch {}
  }

  return (
    <div className={styles.section}>
      <h2 className={styles.title}>Reviews ({reviews.length})</h2>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.stars}>
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              type="button"
              onClick={() => setNewRating(star)}
              className={styles.starBtn}
            >
              <Star
                size={20}
                fill={star <= newRating ? '#fbbf24' : 'none'}
                stroke="#fbbf24"
              />
            </button>
          ))}
        </div>
        <textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Share your thoughts... (optional)"
          rows={3}
          className={styles.textarea}
          maxLength={100}
        />
        <Button type="submit" loading={createApi.loading}>Submit Review</Button>
      </form>

      {reviews.length > 0 ? (
        <div className={styles.list}>
          {reviews.map((review) => (
            <div key={review.id} className={styles.item}>
              <div className={styles.header}>
                <span className={styles.user}>{review.userName}</span>
                <div className={styles.rating}>
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      key={i}
                      size={14}
                      fill={i < review.rating ? '#fbbf24' : 'none'}
                      stroke="#fbbf24"
                    />
                  ))}
                </div>
                <span className={styles.date}>
                  {new Date(review.createdAt).toLocaleDateString()}
                </span>
              </div>
              {review.comment && <p className={styles.comment}>{review.comment}</p>}
            </div>
          ))}
          {totalReviewPages > 1 && (
            <Pagination
              currentPage={reviewPage}
              totalPages={totalReviewPages}
              onPageChange={(p) => { setReviewPage(p); loadReviews(p) }}
            />
          )}
        </div>
      ) : (
        <p className={styles.empty}>No reviews yet. Be the first!</p>
      )}
    </div>
  )
}