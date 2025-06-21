package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.interfaces.ProductInterface;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductInterface {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;

    @Value("${project.image}")
    private String imageDefaultPath;


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow( () -> new ResourceNotFoundException("Category", "categoryId", String.valueOf(categoryId)));

        boolean isProductNotPresent = true;

        List<Product> products = category.getProducts();

        for (Product value: products) {
           if (value.getProductName().equals(productDTO.getProductName())) {
               isProductNotPresent = false;
               break;
           }
        }

        if (isProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);

            product.setImage("default.png");

            product.setCategory(category);

            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());

            product.setSpecialPrice(specialPrice);

            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        } else {
            throw new ResourceNotFoundException("Product", "productName", productDTO.getProductName());
        }



    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        
        Page<Product> productPage = productRepository.findAll(pageDetails);
        
        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (products.isEmpty())
            throw new ResourceNotFoundException("Product", "productId", "No products found");

        ProductResponse productResponse = new ProductResponse();

        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productResponse.isLastPage());
        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow( () -> new ResourceNotFoundException("Category", "categoryId", String.valueOf(categoryId)));

        List<Product> products = productRepository.findByCategoryOrderByPriceAsc(category);

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (products.isEmpty())
            throw new ResourceNotFoundException("Product", "productId", "No products found");

        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword) {

        List<Product> products = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%");
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (products.isEmpty())
            throw new ResourceNotFoundException("Product", "productId", "No products found");

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        return productResponse;

    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {

        // get the existing product

        Product productFromDb = productRepository.findById(productId)
                .orElseThrow( () -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        Product product = modelMapper.map(productDTO, Product.class);
        // update the product info with the one in the request body

        productFromDb.setProductName(product.getProductName());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setSpecialPrice(product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice()));

        Product savedProduct = productRepository.save(productFromDb);

        // update product in cart



        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow( () -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        productRepository.delete(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // get the existing product

        Product productFromDb = productRepository.findById(productId)
                .orElseThrow( () -> new ResourceNotFoundException("Product", "productId", String.valueOf(productId)));

        String path = imageDefaultPath+productFromDb.getProductId()+"/";

        String fileName = fileService.uploadImage(path, image);

        // upload the product image to server
        productFromDb.setImage(fileName);

        // Save updated product
        Product updatedProduct = productRepository.save(productFromDb);

        // Get file name of uploaded image
        return modelMapper.map(updatedProduct, ProductDTO.class);

    }

}
