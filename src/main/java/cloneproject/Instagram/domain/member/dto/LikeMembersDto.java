package cloneproject.Instagram.domain.member.dto;

import com.querydsl.core.annotations.QueryProjection;

import cloneproject.Instagram.domain.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

// @Data
@Getter
@NoArgsConstructor
public class LikeMembersDto {

	private MemberDto member;
	private boolean isFollowing;
	private boolean isFollower;
	private boolean hasStory;

	public void setHasStory(boolean hasStory) {
		this.hasStory = hasStory;
	}

	@QueryProjection
	public LikeMembersDto(Member member, boolean isFollowing, boolean isFollower) {
		this.member = new MemberDto(member);
		this.isFollowing = isFollowing;
		this.isFollower = isFollower;
		this.hasStory = false;
	}

}
