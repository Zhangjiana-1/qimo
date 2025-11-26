package com.example.qimo.service.impl;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Category;
import com.example.qimo.repository.BookRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.data.domain.PageImpl;
import com.example.qimo.service.BookService;
import com.example.qimo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, CategoryService categoryService, JdbcTemplate jdbcTemplate) {
        this.bookRepository = bookRepository;
        this.categoryService = categoryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Page<Book> getBooks(Pageable pageable) {
        try {
            return bookRepository.findAll(pageable);
        } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException ex) {
            // 如果存在因category引用缺失导致的读取错误，使用原生SQL降级读取（不尝试装载Category实体）
            long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class);
            int pageSize = pageable.getPageSize();
            int pageNumber = pageable.getPageNumber();
            int offset = pageNumber * pageSize;
            String sql = "SELECT id, title, author, isbn, publisher, description, cover_image, year, created_at, updated_at FROM books ORDER BY id DESC LIMIT ? OFFSET ?";
            RowMapper<Book> mapper = (rs, rowNum) -> {
                Book b = new Book();
                b.setId(rs.getLong("id"));
                b.setTitle(rs.getString("title"));
                b.setAuthor(rs.getString("author"));
                b.setIsbn(rs.getString("isbn"));
                b.setPublisher(rs.getString("publisher"));
                b.setDescription(rs.getString("description"));
                b.setCoverImage(rs.getString("cover_image"));
                int year = rs.getInt("year");
                if (!rs.wasNull()) b.setYear(year);
                b.setCreatedAt(rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime());
                b.setUpdatedAt(rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime());
                // 不装载 category，保持为 null
                b.setCategory(null);
                return b;
            };
            java.util.List<Book> rows = jdbcTemplate.query(sql, new Object[]{pageSize, offset}, mapper);
            return new PageImpl<>(rows, pageable, total);
        }
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        try {
            Optional<Book> opt = bookRepository.findById(id);
            if (opt.isPresent()) {
                Book b = opt.get();
                // 防御性加载category，避免因找不到category实体导致页面渲染时抛出异常
                try {
                    if (b.getCategory() != null) {
                        b.getCategory().getName(); // 尝试访问以触发懒加载
                    }
                } catch (RuntimeException inner) {
                    // 出现加载异常（例如EntityNotFound），将category置空以保证渲染安全
                    b.setCategory(null);
                }
                return Optional.of(b);
            }
            return Optional.empty();
        } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException ex) {
            // 降级到原生查询，确保Book能被读取（不依赖Category表）
            String sql = "SELECT id, title, author, isbn, publisher, description, cover_image, year, created_at, updated_at FROM books WHERE id = ?";
            try {
                Book b = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                    Book book = new Book();
                    book.setId(rs.getLong("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setPublisher(rs.getString("publisher"));
                    book.setDescription(rs.getString("description"));
                    book.setCoverImage(rs.getString("cover_image"));
                    int year = rs.getInt("year");
                    if (!rs.wasNull()) book.setYear(year);
                    book.setCreatedAt(rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime());
                    book.setUpdatedAt(rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime());
                    book.setCategory(null);
                    return book;
                });
                return Optional.ofNullable(b);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    @Override
    @Transactional
    public Book saveBook(Book book, Long categoryId) {
        // 只有当categoryId有效（不为null且大于0）时，才尝试获取和设置分类
        if (categoryId != null && categoryId > 0) {
            try {
                Category category = categoryService.findById(categoryId);
                book.setCategory(category);
            } catch (Exception e) {
                // 如果获取分类失败，不设置分类，让category保持为null
                // 记录错误但不抛出异常，确保书籍能够保存
                book.setCategory(null);
            }
        } else {
            // 如果categoryId无效，设置category为null
            book.setCategory(null);
        }
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    public boolean existsByIsbnAndIdNot(String isbn, Long id) {
        return bookRepository.existsByIsbnAndIdNot(isbn, id);
    }

    @Override
    public List<Book> getBooksByCategory(Category category) {
        return bookRepository.findByCategory(category);
    }

    @Override
    public Page<Book> getBooksByCategoryId(Long categoryId, Pageable pageable) {
        // 添加对categoryId的检查
        if (categoryId == null || categoryId <= 0) {
            // 如果categoryId无效，返回空的结果集
            return Page.empty(pageable);
        }
        return bookRepository.findByCategoryId(categoryId, pageable);
    }
    
    @Override
    public Page<Book> findBooksByCategory(Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Book> searchBooksByCategory(String keyword, Long categoryId, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findBooksByCategory(categoryId, pageable);
        }
        if (categoryId == null) {
            return bookRepository.findByTitleContainingOrAuthorContaining(keyword, pageable);
        }
        return bookRepository.findByTitleContainingOrAuthorContainingAndCategoryId(keyword, categoryId, pageable);
    }
}