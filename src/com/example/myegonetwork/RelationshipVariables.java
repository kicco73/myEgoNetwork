/**
 * 
 */
package com.example.myegonetwork;

/**
 * @author Valerio Arnaboldi (valerio.arnaboldi@gmail.com)
 *
 */
public class RelationshipVariables {
	
	private Long downloadDate;
	private int posts;
	private Long firstPostDate;
	private int comments;
	private Long firstCommentDate;
	private int likes;
	private Long firstLikeDate;
	//TODO extend this class to represent other Facebook variables
	
	public RelationshipVariables(){
		downloadDate = Long.valueOf(0);
		posts = 0;
		firstPostDate = Long.valueOf(0);
		comments = 0;
		firstCommentDate = Long.valueOf(0);
		likes = 0;
		firstLikeDate = Long.valueOf(0);
	}
	
	public Long getDownloadDate(){
		return downloadDate;
	}
	public void setDownloadDate(Long downloadDate){
		this.downloadDate = downloadDate;
	}
	public int getPosts() {
		return posts;
	}
	public void setPosts(int posts) {
		this.posts = posts;
	}
	public void incPosts(){
		this.posts += 1;
	}
	public Long getFirstPost() {
		return firstPostDate;
	}
	public void setFirstPost(Long firstPost) {
		this.firstPostDate = firstPost;
	}
	public int getComments() {
		return comments;
	}
	public void setComments(int comments) {
		this.comments = comments;
	}
	public void incComments(){
		this.comments += 1;
	}
	public Long getFirstComment() {
		return firstCommentDate;
	}
	public void setFirstComment(Long firstComment) {
		this.firstCommentDate = firstComment;
	}
	public int getLikes() {
		return likes;
	}
	public void setLikes(int likes) {
		this.likes = likes;
	}
	public void incLikes(){
		this.likes += 1;
	}
	public Long getFirstLike() {
		return firstLikeDate;
	}
	public void setFirstLike(Long firstLike) {
		this.firstLikeDate = firstLike;
	}
}
