package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Blog;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.BlogRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Profile("!prod")
@Order(7)

public class DemoBlogInitializer implements ApplicationRunner {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Chỉ tạo dữ liệu nếu bảng blog đang trống
        if (blogRepository.count() > 0) {
            return;
        }

        // Tìm một user có vai trò STAFF hoặc ADMIN để làm tác giả
        User author = userRepository.findFirstByRole(UserRole.MedicalStaff)
                .orElseGet(() -> userRepository.findFirstByRole(UserRole.SchoolAdmin)
                        .orElse(null));

        if (author == null) {
            System.out.println("Cannot find STAFF or ADMIN user to be the author of blog posts. Skipping blog initialization.");
            return;
        }

        Blog blog1 = new Blog();
        blog1.setTitle("Thải độc thận bằng các món ngon lành rẻ tiền sau");
        blog1.setContent("""
                <p data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}"><strong>Ăn g&igrave; tốt cho thận v&agrave; gan? Gợi &yacute; thực phẩm gi&uacute;p thải độc thận hiệu quả</strong></p>
                <p data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">Gan v&agrave;&nbsp;<strong><a title="thận" href="https://laodong.vn/suc-khoe/6-loai-thuc-pham-giup-than-khoe-manh-loc-mau-hieu-qua-hon-1524109.ldo" target="_blank" rel="noopener" data-t="{&quot;n&quot;:&quot;destination&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:1,&quot;c.t&quot;:7}">thận</a></strong> l&agrave; hai cơ quan chủ lực trong việc lọc độc tố v&agrave; duy tr&igrave; sự c&acirc;n bằng cho cơ thể. Tuy nhi&ecirc;n, th&oacute;i quen ăn uống thiếu l&agrave;nh mạnh, m&ocirc;i trường &ocirc; nhiễm hay d&ugrave;ng thuốc k&eacute;o d&agrave;i c&oacute; thể khiến gan thận bị qu&aacute; tải, dẫn đến suy giảm chức năng. Vậy ăn g&igrave; tốt cho thận v&agrave; gan, đặc biệt trong bối cảnh nhiều người t&igrave;m c&aacute;ch thải độc thận an to&agrave;n từ tự nhi&ecirc;n?</p>
                <p class="continue-read-break" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">C&aacute;c chuy&ecirc;n gia dinh dưỡng khuyến nghị một số nh&oacute;m thực phẩm c&oacute; khả năng hỗ trợ thải độc gan thận, gi&uacute;p giảm g&aacute;nh nặng lọc độc tố v&agrave; tăng cường chức năng cơ thể.</p>
                <p data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}"><strong>1. Nước lọc v&agrave; tr&aacute;i c&acirc;y chứa nhiều nước, cơ bản nhưng hiệu quả</strong></p>
                <p data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">Kh&ocirc;ng c&oacute; thực phẩm n&agrave;o gi&uacute;p&nbsp;<strong><a title="thải độc" href="https://laodong.vn/dinh-duong-am-thuc/3-cach-thai-doc-don-gian-tai-nha-hieu-qua-sau-tet-1458255.ldo" target="_blank" rel="noopener" data-t="{&quot;n&quot;:&quot;destination&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:1,&quot;c.t&quot;:7}">thải độc</a></strong>&nbsp;thận hiệu quả hơn nước. Cung cấp đủ 2 &ndash; 2,5 l&iacute;t nước mỗi ng&agrave;y gi&uacute;p thận dễ d&agrave;ng đ&agrave;o thải ure, creatinine v&agrave; c&aacute;c chất chuyển h&oacute;a kh&aacute;c. B&ecirc;n cạnh đ&oacute;, c&aacute;c loại tr&aacute;i c&acirc;y như dưa hấu, dưa leo, cam, chanh, d&acirc;u t&acirc;y&hellip; đều gi&agrave;u nước v&agrave; vitamin C, gi&uacute;p m&aacute;t gan, lợi tiểu tự nhi&ecirc;n.</p>
                <p class="" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}"><strong>2. Rau l&aacute; xanh, &ldquo;thuốc bổ&rdquo; cho gan thận yếu</strong></p>
                <p data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">Rau bina (cải b&oacute; x&ocirc;i), cải xoăn, cải b&oacute; x&ocirc;i, mồng tơi hay rau m&aacute; l&agrave; những thức ăn tốt cho gan thận nhờ gi&agrave;u chlorophyll &ndash; hợp chất hỗ trợ trung h&ograve;a độc tố, giảm g&aacute;nh nặng cho gan. Đặc biệt, rau m&aacute; v&agrave; diệp hạ ch&acirc;u c&ograve;n c&oacute; t&aacute;c dụng m&aacute;t gan bổ thận, được d&ugrave;ng trong nhiều b&agrave;i thuốc d&acirc;n gian v&agrave; y học cổ truyền.</p>
                """
        );
        blog1.setThumbnail("https://res.cloudinary.com/ddytmhuz4/image/upload/v1752634049/school_proofs/blog-thumbnails/thumbnail_1752634047608_4933c148.jpg");
        blog1.setDescription("Bạn băn khoăn ăn gì để thải độc thận, bảo vệ gan? Dưới đây là những thực phẩm giúp làm sạch gan thận tự nhiên, được các chuyên gia khuyên dùng.");
        blog1.setSlug("thai-oc-than-bang-cac-mon-ngon-lanh-re-tien-sau");
        blog1.setAuthor(author);
        blog1.setStatus(BlogStatus.PUBLIC);
        blog1.setCategory(BlogCategory.HEALTH_NEWS);
        blog1.setCreatedAt(java.time.LocalDateTime.now());

        blogRepository.save(blog1);

        Blog blog2 = new Blog();
        blog2.setTitle("Lợi, hại của việc ăn một bữa mỗi ngày");
        blog2.setContent("""
                <p class="Normal" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}"><strong>Ăn &iacute;t hơn để giảm c&acirc;n</strong></p>
                <p class="Normal" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">Chuy&ecirc;n gia dinh dưỡng Jenna Hope cho biết: "Ở những người kh&ocirc;ng c&oacute; vấn đề sức khỏe tiềm ẩn, việc ti&ecirc;u thụ &iacute;t calo hơn mức cơ thể ti&ecirc;u hao sẽ dẫn đến giảm c&acirc;n. Những người ăn theo chế độ OMAD thường nạp v&agrave;o &iacute;t calo hơn so với nhu cầu duy tr&igrave; c&acirc;n nặng".</p>
                <p class="Normal" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">April Morgan, trưởng bộ phận dinh dưỡng tại Artah, cho rằng nhịn ăn hoặc gi&atilde;n c&aacute;ch giữa c&aacute;c bữa c&oacute; thể gi&uacute;p giảm vi&ecirc;m, cải thiện ti&ecirc;u h&oacute;a v&agrave; tăng khả năng tập trung.</p>
                <p class="Normal" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}"><strong>Nguy&ecirc;n tắc khi thực hiện OMAD</strong></p>
                <p class="Normal" data-t="{&quot;n&quot;:&quot;blueLinks&quot;,&quot;t&quot;:13,&quot;a&quot;:&quot;click&quot;,&quot;b&quot;:76}">Người theo OMAD thường kh&ocirc;ng ti&ecirc;u thụ bất kỳ dạng calo n&agrave;o ngo&agrave;i bữa ch&iacute;nh, kể cả đường hay sữa trong tr&agrave; v&agrave; c&agrave; ph&ecirc;. Người theo chế độ OMAD c&oacute; thể d&ugrave;ng tr&agrave; đen, tr&agrave; thảo mộc hoặc c&agrave; ph&ecirc; đen kh&ocirc;ng đường trong thời gian nhịn ăn. Uống đủ nước &ndash; khoảng 2,5 l&iacute;t mỗi ng&agrave;y &ndash; l&agrave; rất quan trọng để duy tr&igrave; năng lượng.</p>
                """
        );
        blog2.setThumbnail("https://res.cloudinary.com/ddytmhuz4/image/upload/v1752767958/school_proofs/blog-thumbnails/thumbnail_1752767956262_d7d6492e.png");
        blog2.setAuthor(author);
        blog2.setSlug("loi-hai-cua-viec-an-mot-bua-moi-ngay");
        blog2.setDescription("Ăn một bữa mỗi ngày (OMAD) đang trở thành xu hướng giảm cân phổ biến. Tuy nhiên, chế độ ăn này có thể mang lại cả lợi ích và rủi ro cho sức khỏe.");
        blog2.setStatus(BlogStatus.PRIVATE);
        blog2.setCategory(BlogCategory.HEALTH_NEWS);
        blog2.setCreatedAt(java.time.LocalDateTime.now());

        blogRepository.save(blog2);
    }
}