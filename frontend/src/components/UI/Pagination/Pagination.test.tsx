import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Pagination from './Pagination'

describe('Pagination', () => {
  it('renders nothing when there is one page or fewer', () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={1} onPageChange={vi.fn()} />
    )
    expect(container).toBeEmptyDOMElement()
  })

  it('renders a page button for every page when the total fits within the visible window', () => {
    render(<Pagination currentPage={0} totalPages={3} onPageChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: '1' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '2' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '3' })).toBeInTheDocument()
  })

  it('calls onPageChange with the target page when a page button is clicked', async () => {
    const onPageChange = vi.fn()
    render(<Pagination currentPage={0} totalPages={3} onPageChange={onPageChange} />)

    await userEvent.click(screen.getByRole('button', { name: '2' }))

    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('disables the previous/first buttons on the first page', () => {
    render(<Pagination currentPage={0} totalPages={5} onPageChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: '«' })).toBeDisabled()
    expect(screen.getByRole('button', { name: '←' })).toBeDisabled()
    expect(screen.getByRole('button', { name: '»' })).not.toBeDisabled()
  })

  it('disables the next/last buttons on the last page', () => {
    render(<Pagination currentPage={4} totalPages={5} onPageChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: '»' })).toBeDisabled()
    expect(screen.getByRole('button', { name: '→' })).toBeDisabled()
    expect(screen.getByRole('button', { name: '«' })).not.toBeDisabled()
  })

  it('collapses far-away pages behind an ellipsis when there are many pages', () => {
    render(<Pagination currentPage={0} totalPages={10} onPageChange={vi.fn()} />)

    expect(screen.getByText('…')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '10' })).toBeInTheDocument()
  })

  it('renders a Load More button in load-more variant and advances the page on click', async () => {
    const onPageChange = vi.fn()
    render(
      <Pagination currentPage={0} totalPages={3} variant="load-more" onPageChange={onPageChange} />
    )

    const button = screen.getByRole('button', { name: 'Load More' })
    await userEvent.click(button)

    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('disables and relabels the Load More button while loading', () => {
    render(
      <Pagination currentPage={0} totalPages={3} variant="load-more" loading onPageChange={vi.fn()} />
    )

    const button = screen.getByRole('button', { name: 'Loading...' })
    expect(button).toBeDisabled()
  })
})
