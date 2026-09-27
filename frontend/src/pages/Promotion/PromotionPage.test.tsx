import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import PromotionPage from './PromotionPage'
import { useNotification } from '../../context/useNotification'
import { getPromotionBySlug } from '../../api/promotions'
import type { PromotionResponse } from '../../types'

vi.mock('../../context/useNotification')
vi.mock('../../api/promotions')

const DAY = 24 * 60 * 60 * 1000

function promotion(overrides: Partial<PromotionResponse>): PromotionResponse {
  return {
    id: 1,
    slug: 'weekend-sale',
    title: 'Weekend Sale',
    discountPercent: 20,
    startDate: new Date(Date.now() - DAY).toISOString(),
    endDate: new Date(Date.now() + 3 * DAY).toISOString(),
    active: true,
    isCurrentlyActive: true,
    products: [],
    ...overrides,
  }
}

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/promotions/weekend-sale']}>
      <Routes>
        <Route path="/promotions/:slug" element={<PromotionPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('PromotionPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useNotification).mockReturnValue({ showNotification: vi.fn() } as unknown as ReturnType<typeof useNotification>)
  })

  it('shows the discount and days left for a running promotion', async () => {
    vi.mocked(getPromotionBySlug).mockResolvedValue(promotion({}))

    renderPage()

    expect(await screen.findByText('20%')).toBeInTheDocument()
    expect(screen.getByText(/days? left/)).toBeInTheDocument()
  })

  it('hides the discount for an ended promotion', async () => {
    vi.mocked(getPromotionBySlug).mockResolvedValue(promotion({
      startDate: new Date(Date.now() - 10 * DAY).toISOString(),
      endDate: new Date(Date.now() - DAY).toISOString(),
      isCurrentlyActive: false,
    }))

    renderPage()

    expect(await screen.findByText('This promotion has ended')).toBeInTheDocument()
    expect(screen.queryByText('20%')).not.toBeInTheDocument()
  })

  it('hides the discount for a deactivated promotion', async () => {
    vi.mocked(getPromotionBySlug).mockResolvedValue(promotion({ active: false, isCurrentlyActive: false }))

    renderPage()

    expect(await screen.findByText('This promotion has ended')).toBeInTheDocument()
    expect(screen.queryByText('20%')).not.toBeInTheDocument()
  })

  it('shows the start date for an upcoming promotion', async () => {
    vi.mocked(getPromotionBySlug).mockResolvedValue(promotion({
      startDate: new Date(Date.now() + 2 * DAY).toISOString(),
      endDate: new Date(Date.now() + 5 * DAY).toISOString(),
      isCurrentlyActive: false,
    }))

    renderPage()

    expect(await screen.findByText(/^Starts /)).toBeInTheDocument()
    expect(screen.queryByText('20%')).not.toBeInTheDocument()
  })
})
