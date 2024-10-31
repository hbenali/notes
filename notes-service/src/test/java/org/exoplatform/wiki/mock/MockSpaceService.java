package org.exoplatform.wiki.mock;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;

public class MockSpaceService implements SpaceService {

  public Space getSpaceByPrettyName(String spacePrettyName) {
    return null;
  }

  public Space getSpaceByGroupId(String groupId) {
    return null;
  }

  public Space getSpaceById(String spaceId) {
    return null;
  }

  public Space getSpaceByUrl(String spaceUrl) {
    return null;
  }

  public ListAccess<Space> getAllSpacesWithListAccess() {
    return null;
  }

  public ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter) {
    return null;
  }

  public ListAccess<Space> getMemberSpaces(String userId) {
    return null;
  }

  public ListAccess<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public ListAccess<Space> getAccessibleSpacesWithListAccess(String userId) {
    return null;
  }

  public ListAccess<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public ListAccess<Space> getSettingableSpaces(String userId) {
    return null;
  }

  public ListAccess<Space> getSettingabledSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public ListAccess<Space> getInvitedSpacesWithListAccess(String userId) {
    return null;
  }

  public ListAccess<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public ListAccess<Space> getPublicSpacesWithListAccess(String userId) {
    return null;
  }

  public ListAccess<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public ListAccess<Space> getPendingSpacesWithListAccess(String userId) {
    return null;
  }

  public ListAccess<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public Space createSpace(Space space, String creatorUserId) {
    return null;
  }

  public Space updateSpace(Space existingSpace) {
    return null;
  }

  public Space updateSpaceAvatar(Space existingSpace, String username) {
    return null;
  }

  public Space updateSpaceBanner(Space existingSpace, String username) {
    return null;
  }

  public void deleteSpace(Space space) {
    
  }

  public void addPendingUser(Space space, String userId) {
    
  }

  public void removePendingUser(Space space, String userId) {
    
  }

  public boolean isPendingUser(Space space, String userId) {
    return false;
  }

  public void addInvitedUser(Space space, String userId) {
    
  }

  public void removeInvitedUser(Space space, String userId) {
    
  }

  public boolean isInvitedUser(Space space, String userId) {
    return false;
  }

  public void addMember(Space space, String userId) {
    
  }

  public void removeMember(Space space, String userId) {
    
  }

  public boolean isMember(Space space, String userId) {
    return false;
  }

  public void setManager(Space space, String userId, boolean isManager) {
    
  }

  public boolean isManager(Space space, String userId) {
    return false;
  }

  public boolean isOnlyManager(Space space, String userId) {
    return false;
  }

  public boolean hasAccessPermission(Space space, String userId) {
    return false;
  }

  public boolean hasSettingPermission(Space space, String userId) {
    return false;
  }

  public void registerSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    
  }

  public void unregisterSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    
  }

  public List<Space> getAllSpaces() throws SpaceException {
    return null;
  }

  public Space getSpaceByName(String spaceName) throws SpaceException {
    return null;
  }

  public List<Space> getSpacesBySearchCondition(String condition) throws Exception {
    return null;
  }

  public List<Space> getSpaces(String userId) throws SpaceException {
    return null;
  }

  public List<Space> getAccessibleSpaces(String userId) throws SpaceException {
    return null;
  }

  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceException {
    return null;
  }

  public SpaceListAccess getVisibleSpacesWithListAccess(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public SpaceListAccess getUnifiedSearchSpacesWithListAccess(String userId, SpaceFilter spaceFilter) {
    return null;
  }

  public List<Space> getEditableSpaces(String userId) throws SpaceException {
    return null;
  }

  public List<Space> getInvitedSpaces(String userId) throws SpaceException {
    return null;
  }

  public List<Space> getPublicSpaces(String userId) throws SpaceException {
    return null;
  }

  public List<Space> getPendingSpaces(String userId) throws SpaceException {
    return null;
  }

  public Space createSpace(Space space, String creator, String invitedGroupId) throws SpaceException {
    return null;
  }

  public Space createSpace(Space space, String creator, List<Identity> identities) throws SpaceException {
    return null;
  }

  public void createSpace(Space space, boolean isNew) throws SpaceException {
    
  }

  public void renameSpace(Space space, String newDisplayName, String remoteId) throws SpaceException {
    
  }

  public void initApp(Space space) throws SpaceException {
    
  }

  public void initApps(Space space) throws SpaceException {
    
  }

  public void deInitApps(Space space) throws SpaceException {
    
  }

  public void setLeader(Space space, String userId, boolean isLeader) throws SpaceException {
    
  }

  public void setLeader(String spaceId, String userId, boolean isLeader) throws SpaceException {
    
  }

  public boolean isLeader(Space space, String userId) throws SpaceException {
    return false;
  }

  public boolean isLeader(String spaceId, String userId) throws SpaceException {
    return false;
  }

  public boolean isOnlyLeader(Space space, String userId) throws SpaceException {
    return false;
  }

  public boolean isOnlyLeader(String spaceId, String userId) throws SpaceException {
    return false;
  }

  public boolean hasAccessPermission(String spaceId, String userId) throws SpaceException {
    return false;
  }

  public boolean hasEditPermission(Space space, String userId) throws SpaceException {
    return false;
  }

  public boolean hasEditPermission(String spaceId, String userId) throws SpaceException {
    return false;
  }

  public boolean isInvited(Space space, String userId) throws SpaceException {
    return false;
  }

  public boolean isInvited(String spaceId, String userId) throws SpaceException {
    return false;
  }

  public boolean isPending(Space space, String userId) throws SpaceException {
    return false;
  }

  public boolean isPending(String spaceId, String userId) throws SpaceException {
    return false;
  }

  public boolean isIgnored(Space space, String userId) {
    return false;
  }

  public void setIgnored(String spaceId, String userId) {

  }

  public void installApplication(String spaceId, String appId) throws SpaceException {
    
  }

  public void installApplication(Space space, String appId) throws SpaceException {
    
  }

  public void activateApplication(Space space, String appId) throws SpaceException {
    
  }

  public void activateApplication(String spaceId, String appId) throws SpaceException {
    
  }

  public void deactivateApplication(Space space, String appId) throws SpaceException {
    
  }

  public void deactivateApplication(String spaceId, String appId) throws SpaceException {
    
  }

  public void removeApplication(Space space, String appId, String appName) throws SpaceException {
    
  }

  public void removeApplication(String spaceId, String appId, String appName) throws SpaceException {
    
  }

  public List<Space> getLastAccessedSpace(String remoteId, String appId, int offset, int limit) throws SpaceException {
    return null;
  }

  public List<Space> getLastSpaces(int limit) {
    return null;
  }

  public ListAccess<Space> getLastAccessedSpace(String remoteId, String appId) {
    return null;
  }

  public void requestJoin(Space space, String userId) throws SpaceException {
    
  }

  public void requestJoin(String spaceId, String userId) throws SpaceException {
    
  }

  public void revokeRequestJoin(Space space, String userId) throws SpaceException {
    
  }

  public void revokeRequestJoin(String spaceId, String userId) throws SpaceException {
    
  }

  public void inviteMember(Space space, String userId) throws SpaceException {
    
  }

  public void inviteMember(String spaceId, String userId) throws SpaceException {
    
  }

  public void revokeInvitation(Space space, String userId) throws SpaceException {
    
  }

  public void revokeInvitation(String spaceId, String userId) throws SpaceException {
    
  }

  public void acceptInvitation(Space space, String userId) throws SpaceException {
    
  }

  public void acceptInvitation(String spaceId, String userId) throws SpaceException {
    
  }

  public void denyInvitation(Space space, String userId) throws SpaceException {
    
  }

  public void denyInvitation(String spaceId, String userId) throws SpaceException {
    
  }

  public void validateRequest(Space space, String userId) throws SpaceException {
    
  }

  public void validateRequest(String spaceId, String userId) throws SpaceException {
    
  }

  public void declineRequest(Space space, String userId) throws SpaceException {
    
  }

  public void declineRequest(String spaceId, String userId) throws SpaceException {
    
  }

  public void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    
  }

  public void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    
  }

  public ListAccess<Space> getVisitedSpaces(String remoteId, String appId) {
    return null;
  }

  public boolean isSuperManager(String userId) {
    return false;
  }

}
