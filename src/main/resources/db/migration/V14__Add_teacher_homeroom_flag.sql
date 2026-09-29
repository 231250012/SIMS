-- 教师是否班主任：非班主任仅可改成绩，不可改学生其他信息
ALTER TABLE `user`
    ADD COLUMN is_homeroom_teacher BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT '教师是否班主任；非班主任仅可改成绩' AFTER role;

-- 已负责班级的既有教师默认视为班主任，保持演示账号行为一致
UPDATE `user` u
SET u.is_homeroom_teacher = TRUE
WHERE u.role = 'TEACHER'
  AND EXISTS (SELECT 1 FROM class c WHERE c.teacher_id = u.id);
