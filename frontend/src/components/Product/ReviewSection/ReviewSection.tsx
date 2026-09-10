import { useState, useEffect } from "react";
import { Star, Pencil, Trash2 } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import { useApi } from "../../../hooks/common/useApi";
import * as reviewsApi from "../../../api/reviews";
import { useNotification } from "../../../context/useNotification";
import Button from "../../UI/Button/Button";
import Pagination from "../../UI/Pagination/Pagination";
import Modal from "../../UI/Modal/Modal";
import type { ReviewResponse, ReviewReplyResponse } from "../../../types";
import styles from "./ReviewSection.module.css";

interface Props {
  productId: number;
}

export default function ReviewSection({ productId }: Props) {
  const { user, isAdmin } = useAuth();
  const { showNotification } = useNotification();
  const createApi = useApi<ReviewResponse>();
  const updateApi = useApi<ReviewResponse>();
  const replyApi = useApi<ReviewReplyResponse>();
  const updateReplyApi = useApi<ReviewReplyResponse>();
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [reviewPage, setReviewPage] = useState(0);
  const [totalReviewPages, setTotalReviewPages] = useState(0);
  const [newRating, setNewRating] = useState(5);
  const [newComment, setNewComment] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editRating, setEditRating] = useState(5);
  const [editComment, setEditComment] = useState("");
  const [replyMessage, setReplyMessage] = useState("");
  const [replyingId, setReplyingId] = useState<number | null>(null);
  const [editingReplyId, setEditingReplyId] = useState<number | null>(null);
  const [editReplyMessage, setEditReplyMessage] = useState("");
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteReplyId, setDeleteReplyId] = useState<number | null>(null);

  const loadReviews = (page: number) => {
    reviewsApi.getReviews(productId, page).then((res) => {
      setReviews(res.content);
      setTotalReviewPages(res.page.totalPages);
    });
  };

  useEffect(() => {
    loadReviews(0);
  }, [productId]);

  const showError = (err: unknown, fallback: string) => {
    const message =
      (err as { response?: { data?: { message?: string } } })?.response?.data
        ?.message || fallback;
    showNotification(message, "error");
  };

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      await createApi.execute(() =>
        reviewsApi.createReview({
          productId,
          rating: newRating,
          comment: newComment || undefined,
        }),
      );
      setNewComment("");
      setNewRating(5);
      showNotification("Review submitted", "success");
      loadReviews(reviewPage);
    } catch (err: unknown) {
      showError(err, "Failed to submit review");
    }
  };

  const handleUpdate = async (reviewId: number) => {
    try {
      await updateApi.execute(() =>
        reviewsApi.updateReview(reviewId, {
          productId,
          rating: editRating,
          comment: editComment || undefined,
        }),
      );
      setEditingId(null);
      showNotification("Review updated", "success");
      loadReviews(reviewPage);
    } catch (err: unknown) {
      showError(err, "Failed to update review");
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await reviewsApi.deleteReview(deleteId);
      setDeleteId(null);
      showNotification("Review deleted", "success");
      loadReviews(reviewPage);
    } catch (err: unknown) {
      showError(err, "Failed to delete review");
    }
  };

  const handleReply = async (reviewId: number) => {
    if (!replyMessage.trim()) return;
    try {
      await replyApi.execute(() =>
        reviewsApi.addReply(reviewId, { message: replyMessage }),
      );
      setReplyMessage("");
      setReplyingId(null);
      showNotification("Reply added", "success");
      loadReviews(reviewPage);
    } catch (err: unknown) {
      showError(err, "Failed to add reply");
    }
  };

  const handleUpdateReply = async () => {
    if (!editingReplyId || !editReplyMessage.trim()) return;
    try {
      await updateReplyApi.execute(() =>
        reviewsApi.updateReply(editingReplyId, { message: editReplyMessage }),
      );
      setEditingReplyId(null);
      setEditReplyMessage("");
      showNotification("Reply updated", "success");
      loadReviews(reviewPage);
    } catch (err: unknown) {
      showError(err, "Failed to update reply");
    }
  };

  const handleDeleteReply = async () => {
    if (!deleteReplyId) return;
    try {
      await reviewsApi.deleteReply(deleteReplyId);
      setDeleteReplyId(null);
      showNotification("Reply deleted", "success");
      loadReviews(reviewPage);
    } catch (err: unknown) {
      showError(err, "Failed to delete reply");
    }
  };

  const startEdit = (review: ReviewResponse) => {
    setEditingId(review.id);
    setEditRating(review.rating);
    setEditComment(review.comment || "");
  };

  const startEditReply = (reply: ReviewReplyResponse) => {
    setEditingReplyId(reply.id);
    setEditReplyMessage(reply.message);
  };

  return (
    <div className={styles.section}>
      <h2 className={styles.title}>Reviews ({reviews.length})</h2>
      {user && (
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.stars}>
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                onClick={() => setNewRating(star)}
                className={styles.starBtn}
                aria-label={`Rate ${star} star${star > 1 ? "s" : ""}`}
              >
                <Star
                  size={20}
                  fill={star <= newRating ? "var(--color-rating)" : "none"}
                  stroke="var(--color-rating)"
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
          <Button type="submit" loading={createApi.loading}>
            Submit Review
          </Button>
        </form>
      )}
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
                      fill={i < review.rating ? "var(--color-rating)" : "none"}
                      stroke="var(--color-rating)"
                    />
                  ))}
                </div>
                <span className={styles.date}>
                  {new Date(review.createdAt).toLocaleDateString()}
                  {review.updatedAt &&
                    review.updatedAt !== review.createdAt && (
                      <span className={styles.edited}> (edited)</span>
                    )}
                </span>
                {review.userId === user?.id && (
                  <div className={styles.actions}>
                    <button
                      type="button"
                      onClick={() => startEdit(review)}
                      className={styles.editBtn}
                      aria-label="Edit review"
                    >
                      <Pencil size={14} />
                    </button>
                    <button
                      type="button"
                      onClick={() => setDeleteId(review.id)}
                      className={styles.deleteBtn}
                      aria-label="Delete review"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}
              </div>
              {editingId === review.id ? (
                <div className={styles.editForm}>
                  <div className={styles.stars}>
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        type="button"
                        onClick={() => setEditRating(star)}
                        className={styles.starBtn}
                        aria-label={`Rate ${star} star${star > 1 ? "s" : ""}`}
                      >
                        <Star
                          size={16}
                          fill={star <= editRating ? "var(--color-rating)" : "none"}
                          stroke="var(--color-rating)"
                        />
                      </button>
                    ))}
                  </div>
                  <textarea
                    value={editComment}
                    onChange={(e) => setEditComment(e.target.value)}
                    rows={2}
                    className={styles.textarea}
                    maxLength={100}
                  />
                  <div className={styles.editActions}>
                    <Button
                      onClick={() => setEditingId(null)}
                      variant="secondary"
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={() => handleUpdate(review.id)}
                      loading={updateApi.loading}
                    >
                      Save
                    </Button>
                  </div>
                </div>
              ) : (
                review.comment && (
                  <p className={styles.comment}>{review.comment}</p>
                )
              )}
              {review.replies && review.replies.length > 0 && (
                <div className={styles.replies}>
                  {review.replies.map((reply) => (
                    <div key={reply.id} className={styles.reply}>
                      <div className={styles.replyHeader}>
                        <span className={styles.replyAuthor}>
                          {reply.authorName}
                        </span>
                        <span className={styles.replyDate}>
                          {new Date(reply.createdAt).toLocaleDateString()}
                        </span>
                        {isAdmin && (
                          <div className={styles.actions}>
                            <button
                              type="button"
                              onClick={() => startEditReply(reply)}
                              className={styles.editBtn}
                              aria-label="Edit reply"
                            >
                              <Pencil size={12} />
                            </button>
                            <button
                              type="button"
                              onClick={() => setDeleteReplyId(reply.id)}
                              className={styles.deleteBtn}
                              aria-label="Delete reply"
                            >
                              <Trash2 size={12} />
                            </button>
                          </div>
                        )}
                      </div>
                      {editingReplyId === reply.id ? (
                        <div className={styles.editForm}>
                          <textarea
                            value={editReplyMessage}
                            onChange={(e) =>
                              setEditReplyMessage(e.target.value)
                            }
                            rows={2}
                            className={styles.textarea}
                            maxLength={100}
                          />
                          <div className={styles.editActions}>
                            <Button
                              onClick={() => setEditingReplyId(null)}
                              variant="secondary"
                            >
                              Cancel
                            </Button>
                            <Button
                              onClick={handleUpdateReply}
                              loading={updateReplyApi.loading}
                            >
                              Save
                            </Button>
                          </div>
                        </div>
                      ) : (
                        <p className={styles.replyMessage}>{reply.message}</p>
                      )}
                    </div>
                  ))}
                </div>
              )}
              {isAdmin && (
                <div className={styles.replyForm}>
                  {replyingId === review.id ? (
                    <>
                      <textarea
                        value={replyMessage}
                        onChange={(e) => setReplyMessage(e.target.value)}
                        placeholder="Reply to this review..."
                        rows={2}
                        className={styles.textarea}
                        maxLength={100}
                      />
                      <div className={styles.editActions}>
                        <Button
                          onClick={() => {
                            setReplyingId(null);
                            setReplyMessage("");
                          }}
                          variant="secondary"
                        >
                          Cancel
                        </Button>
                        <Button
                          onClick={() => handleReply(review.id)}
                          loading={replyApi.loading}
                        >
                          Reply
                        </Button>
                      </div>
                    </>
                  ) : (
                    <button
                      onClick={() => setReplyingId(review.id)}
                      className={styles.replyBtn}
                    >
                      Reply
                    </button>
                  )}
                </div>
              )}
            </div>
          ))}
          {totalReviewPages > 1 && (
            <Pagination
              currentPage={reviewPage}
              totalPages={totalReviewPages}
              onPageChange={(p) => {
                setReviewPage(p);
                loadReviews(p);
              }}
            />
          )}
        </div>
      ) : (
        <p className={styles.empty}>No reviews yet. Be the first!</p>
      )}
      <Modal
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        title="Delete Review"
      >
        <p>Are you sure you want to delete your review?</p>
        <div className={styles.editActions}>
          <Button onClick={() => setDeleteId(null)} variant="secondary">
            Cancel
          </Button>
          <Button onClick={handleDelete} variant="danger">
            Delete
          </Button>
        </div>
      </Modal>
      <Modal
        isOpen={!!deleteReplyId}
        onClose={() => setDeleteReplyId(null)}
        title="Delete Reply"
      >
        <p>Are you sure you want to delete this reply?</p>
        <div className={styles.editActions}>
          <Button onClick={() => setDeleteReplyId(null)} variant="secondary">
            Cancel
          </Button>
          <Button onClick={handleDeleteReply} variant="danger">
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
