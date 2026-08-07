import { useState, useMemo } from 'react';

interface UsePaginationOptions {
  totalItems: number;
  pageSize?: number;
}

export function usePagination({ totalItems, pageSize = 15 }: UsePaginationOptions) {
  const [currentPage, setCurrentPage] = useState(1);

  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  const start = (currentPage - 1) * pageSize;
  const end = start + pageSize;

  const goTo = (page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)));
  };

  const next = () => {
    if (currentPage < totalPages) setCurrentPage((p) => p + 1);
  };

  const prev = () => {
    if (currentPage > 1) setCurrentPage((p) => p - 1);
  };

  const pages = useMemo(() => {
    const result: number[] = [];
    for (let i = 1; i <= totalPages; i++) {
      result.push(i);
    }
    return result;
  }, [totalPages]);

  return { currentPage, totalPages, start, end, goTo, next, prev, pages, pageSize };
}
