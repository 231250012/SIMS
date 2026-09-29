package com.sims.service;

import com.sims.dto.AssignTeacherClassRequest;
import com.sims.dto.TeacherAdminResponse;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.repository.ClassRepository;
import com.sims.repository.UserRepository;
import com.sims.security.annotation.RequireRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherAdminService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherAdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @RequireRole({UserRole.ADMIN})
    @Transactional(readOnly = true)
    public List<TeacherAdminResponse> listTeachers() {
        return userRepository.findAllTeachers().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 将教师调整为负责指定班级（原班的 teacher 清空，目标班绑定该教师）
     */
    @RequireRole({UserRole.ADMIN})
    public TeacherAdminResponse assignClass(Long teacherId, AssignTeacherClassRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("目标用户不是教师");
        }

        com.sims.entity.Class target = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));

        // 解除该教师当前负责的其他班级
        List<com.sims.entity.Class> current = classRepository.findByTeacherId(teacherId);
        for (com.sims.entity.Class c : current) {
            if (!c.getId().equals(target.getId())) {
                c.setTeacher(null);
                classRepository.save(c);
            }
        }

        // 若目标班原有其他教师，先解绑
        if (target.getTeacher() != null && !target.getTeacher().getId().equals(teacherId)) {
            logger.info("班级 {} 原教师 {} 将被替换为 {}",
                    target.getName(), target.getTeacher().getUsername(), teacher.getUsername());
        }
        target.setTeacher(teacher);
        classRepository.save(target);

        if (request.getIsHomeroomTeacher() != null) {
            teacher.setIsHomeroomTeacher(request.getIsHomeroomTeacher());
            userRepository.save(teacher);
        }

        return toResponse(teacher);
    }

    @RequireRole({UserRole.ADMIN})
    public void deleteTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("只能删除教师账号");
        }

        List<com.sims.entity.Class> managed = classRepository.findByTeacherId(teacherId);
        for (com.sims.entity.Class c : managed) {
            c.setTeacher(null);
            classRepository.save(c);
        }

        userRepository.delete(teacher);
        logger.info("已删除教师账号: {}", teacher.getUsername());
    }

    @RequireRole({UserRole.ADMIN})
    public TeacherAdminResponse updateHomeroomFlag(Long teacherId, boolean isHomeroom) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("目标用户不是教师");
        }
        teacher.setIsHomeroomTeacher(isHomeroom);
        return toResponse(userRepository.save(teacher));
    }

    private TeacherAdminResponse toResponse(User teacher) {
        TeacherAdminResponse r = new TeacherAdminResponse();
        r.setId(teacher.getId());
        r.setUsername(teacher.getUsername());
        r.setIsHomeroomTeacher(teacher.getIsHomeroomTeacher());
        r.setCreateTime(teacher.getCreateTime());

        List<com.sims.entity.Class> classes = classRepository.findByTeacherId(teacher.getId());
        r.setManagedClasses(classes.stream()
                .map(c -> new TeacherAdminResponse.ClassBrief(c.getId(), c.getName(), c.getGrade()))
                .collect(Collectors.toList()));
        return r;
    }
}
