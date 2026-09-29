export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'

export interface RoleRouterConfig {
  admin: string
  teacher: string
  student: string
}

export interface NavigationItem {
  title: string
  path: string
  icon: string
  roles: string[]
}

export class RoleRouter {
  private static readonly DEFAULT_ROUTES: RoleRouterConfig = {
    admin: '/dashboard',
    teacher: '/teacher/dashboard',
    student: '/student/dashboard'
  }

  private static readonly ROLE_PERMISSIONS: Record<UserRole, string[]> = {
    ADMIN: [
      '/dashboard',
      '/students',
      '/students/:id',
      '/students/:id/edit',
      '/statistics',
      '/admin/teachers',
      '/admin/classes',
      '/profile',
      '/teacher/dashboard',
      '/teacher/classes',
      '/teacher/classes/:id',
      '/student/dashboard',
      '/student/grades'
    ],
    TEACHER: [
      '/teacher/dashboard',
      '/teacher/classes',
      '/teacher/classes/:id',
      '/teacher/students/:id',
      '/students',
      '/students/:id',
      '/students/:id/edit',
      '/profile'
    ],
    STUDENT: [
      '/student/dashboard',
      '/student/grades',
      '/student/notifications',
      '/profile'
    ]
  }

  /**
   * 根据用户角色获取默认路由
   */
  static getDefaultRoute(userRole: UserRole): string {
    switch (userRole) {
      case 'ADMIN':
        return this.DEFAULT_ROUTES.admin
      case 'TEACHER':
        return this.DEFAULT_ROUTES.teacher
      case 'STUDENT':
        return this.DEFAULT_ROUTES.student
      default:
        return '/login'
    }
  }

  /**
   * 检查用户是否有权限访问指定路由
   */
  static canAccessRoute(userRole: UserRole | undefined, route: string): boolean {
    if (!userRole) return false

    const allowedRoutes = this.ROLE_PERMISSIONS[userRole] || []
    
    // 检查精确匹配
    if (allowedRoutes.includes(route)) {
      return true
    }

    // 检查动态路由匹配
    return allowedRoutes.some(allowedRoute => {
      if (allowedRoute.includes(':')) {
        const pattern = allowedRoute.replace(/:[^/]+/g, '[^/]+')
        const regex = new RegExp(`^${pattern}$`)
        return regex.test(route)
      }
      return false
    })
  }

  /**
   * 获取用户角色可访问的所有路由
   */
  static getAllowedRoutes(userRole: UserRole): string[] {
    return this.ROLE_PERMISSIONS[userRole] || []
  }

  /**
   * 检查路由是否需要特定角色权限
   */
  static getRequiredRole(route: string): UserRole | null {
    // 检查每个角色的权限
    for (const [role, routes] of Object.entries(this.ROLE_PERMISSIONS)) {
      if (routes.includes(route)) {
        return role as UserRole
      }
    }
    return null
  }

  /**
   * 获取角色友好名称
   */
  static getRoleName(role: UserRole): string {
    const roleNames: Record<UserRole, string> = {
      ADMIN: '管理员',
      TEACHER: '教师',
      STUDENT: '学生'
    }
    return roleNames[role] || '未知角色'
  }

  /**
   * 检查是否为角色专用路由
   */
  static isRoleSpecificRoute(route: string): boolean {
    return route.startsWith('/teacher/') || 
           route.startsWith('/student/') || 
           route.startsWith('/admin/')
  }

  /**
   * 从路由路径提取角色
   */
  static extractRoleFromRoute(route: string): UserRole | null {
    if (route.startsWith('/teacher/')) return 'TEACHER'
    if (route.startsWith('/student/')) return 'STUDENT'
    if (route.startsWith('/admin/')) return 'ADMIN'
    return null
  }

  /**
   * 获取角色对应的导航菜单项
   */
  static getNavigationItems(userRole: UserRole): NavigationItem[] {
    const commonItems: NavigationItem[] = [
      {
        title: '个人资料',
        path: '/profile',
        icon: 'User',
        roles: ['ADMIN', 'TEACHER', 'STUDENT']
      }
    ]

    const roleSpecificItems: Record<UserRole, NavigationItem[]> = {
      ADMIN: [
        {
          title: '系统仪表板',
          path: '/dashboard',
          icon: 'Odometer',
          roles: ['ADMIN']
        },
        {
          title: '学生管理',
          path: '/students',
          icon: 'User',
          roles: ['ADMIN']
        },
        {
          title: '教师管理',
          path: '/admin/teachers',
          icon: 'UserFilled',
          roles: ['ADMIN']
        },
        {
          title: '班级管理',
          path: '/admin/classes',
          icon: 'School',
          roles: ['ADMIN']
        },
        {
          title: '统计分析',
          path: '/statistics',
          icon: 'TrendCharts',
          roles: ['ADMIN']
        }
      ],
      TEACHER: [
        {
          title: '教师仪表板',
          path: '/teacher/dashboard',
          icon: 'Odometer',
          roles: ['TEACHER']
        },
        {
          title: '班级管理',
          path: '/teacher/classes',
          icon: 'School',
          roles: ['TEACHER']
        },
        {
          title: '学生管理',
          path: '/students',
          icon: 'User',
          roles: ['TEACHER']
        }
      ],
      STUDENT: [
        {
          title: '学生仪表板',
          path: '/student/dashboard',
          icon: 'Odometer',
          roles: ['STUDENT']
        },
        {
          title: '我的成绩',
          path: '/student/grades',
          icon: 'TrendCharts',
          roles: ['STUDENT']
        },
        {
          title: '消息中心',
          path: '/student/notifications',
          icon: 'Bell',
          roles: ['STUDENT']
        }
      ]
    }

    return [
      ...(roleSpecificItems[userRole] || []),
      ...commonItems
    ]
  }

  /**
   * 验证用户权限并重定向
   */
  static redirectToRoleDefault(userRole: UserRole): string {
    const defaultRoute = this.getDefaultRoute(userRole)
    return defaultRoute
  }

  /**
   * 记录未授权访问尝试
   */
  static logUnauthorizedAccess(userRole: UserRole, attemptedRoute: string): void {
    console.warn(`Unauthorized access attempt: User with role ${userRole} tried to access ${attemptedRoute}`)
    
    // 在生产环境中，这里可以发送到日志服务
    if (import.meta.env.PROD) {
      // 发送到日志服务的逻辑
    }
  }

  /**
   * 获取权限错误消息
   */
  static getPermissionErrorMessage(userRole: UserRole, attemptedRoute: string): string {
    const roleName = this.getRoleName(userRole)
    return `${roleName}没有权限访问该页面`
  }

  /**
   * 检查路由是否为公开路由（不需要认证）
   */
  static isPublicRoute(route: string): boolean {
    const publicRoutes = ['/login', '/register', '/']
    return publicRoutes.includes(route)
  }

  /**
   * 获取角色的主页路由
   */
  static getHomeRoute(userRole: UserRole): string {
    return this.getDefaultRoute(userRole)
  }
}

export default RoleRouter