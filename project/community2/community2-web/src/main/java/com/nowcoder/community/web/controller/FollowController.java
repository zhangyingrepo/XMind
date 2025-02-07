package com.nowcoder.community.web.controller;

import com.nowcoder.community.domain.entity.Event;
import com.nowcoder.community.domain.entity.Page;
import com.nowcoder.community.domain.entity.User;
import com.nowcoder.community.service.event.EventProducer;
import com.nowcoder.community.service.event.EventTopicConstants;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.common.util.CommunityConstant;
import com.nowcoder.community.common.util.CommunityUtil;
import com.nowcoder.community.common.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 关注
 */
@Controller
public class FollowController {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event();
        event.setTopic(EventTopicConstants.TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJsonString(0, "已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJsonString(0, "已取消关注!");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("改用户不存在");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int)followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("改用户不存在");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int)followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser()== null) {
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
    }
}
