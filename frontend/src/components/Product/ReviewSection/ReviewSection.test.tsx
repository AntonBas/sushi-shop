import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ReviewSection from './ReviewSection'
import { useAuth } from '../../../context/useAuth'
import { useNotification } from '../../../context/useNotification'
import { getReviews } from '../../../api/reviews'
import type { ReviewResponse } from '../../../types'
import type { Page } from '../../../types/common'

vi.mock('../../../context/useAuth')
vi.mock('../../../context/useNotification')
vi.mock('../../../api/reviews')

function review(id: number): ReviewResponse {
  return { id, userId: id, userName: `User ${id}`, rating: 5, comment: `Comment ${id}`, createdAt: new Date().toISOString() }
}

function page(content: ReviewResponse[], number: number, totalElements: number): Page<ReviewResponse> {
  return { content, page: { size: 5, number, totalElements, totalPages: Math.ceil(totalElements / 5) } }
}

describe('ReviewSection', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({ user: null, isAdmin: false } as unknown as ReturnType<typeof useAuth>)
    vi.mocked(useNotification).mockReturnValue({ showNotification: vi.fn() } as unknown as ReturnType<typeof useNotification>)
  })

  it('falls back to the last non-empty page when the requested page has emptied', async () => {
    const firstPage = [1, 2, 3, 4, 5].map(review)
    vi.mocked(getReviews)
      .mockResolvedValueOnce(page(firstPage, 0, 6))
      .mockResolvedValueOnce(page([], 1, 5))
      .mockResolvedValueOnce(page(firstPage, 0, 5))

    render(<ReviewSection productId={1} />)
    await userEvent.click(await screen.findByRole('button', { name: '2' }))

    await waitFor(() => expect(getReviews).toHaveBeenLastCalledWith(1, 0, 5, 'createdAt,desc'))
    expect(await screen.findByText('Reviews (5)')).toBeInTheDocument()
    expect(screen.getByText('Comment 1')).toBeInTheDocument()
    expect(screen.queryByText('No reviews yet. Be the first!')).not.toBeInTheDocument()
  })
})
