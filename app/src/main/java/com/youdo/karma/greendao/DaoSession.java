package com.youdo.karma.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.youdo.karma.entity.Conversation;
import com.youdo.karma.entity.Dynamic;
import com.youdo.karma.entity.Gold;
import com.youdo.karma.entity.IMessage;
import com.youdo.karma.entity.NameList;
import com.youdo.karma.entity.Contact;
import com.youdo.karma.entity.ExpressionGroup;
import com.youdo.karma.entity.Expression;

import com.youdo.karma.greendao.ConversationDao;
import com.youdo.karma.greendao.DynamicDao;
import com.youdo.karma.greendao.GoldDao;
import com.youdo.karma.greendao.IMessageDao;
import com.youdo.karma.greendao.NameListDao;
import com.youdo.karma.greendao.ContactDao;
import com.youdo.karma.greendao.ExpressionGroupDao;
import com.youdo.karma.greendao.ExpressionDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig conversationDaoConfig;
    private final DaoConfig dynamicDaoConfig;
    private final DaoConfig goldDaoConfig;
    private final DaoConfig iMessageDaoConfig;
    private final DaoConfig nameListDaoConfig;
    private final DaoConfig contactDaoConfig;
    private final DaoConfig expressionGroupDaoConfig;
    private final DaoConfig expressionDaoConfig;

    private final ConversationDao conversationDao;
    private final DynamicDao dynamicDao;
    private final GoldDao goldDao;
    private final IMessageDao iMessageDao;
    private final NameListDao nameListDao;
    private final ContactDao contactDao;
    private final ExpressionGroupDao expressionGroupDao;
    private final ExpressionDao expressionDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        conversationDaoConfig = daoConfigMap.get(ConversationDao.class).clone();
        conversationDaoConfig.initIdentityScope(type);

        dynamicDaoConfig = daoConfigMap.get(DynamicDao.class).clone();
        dynamicDaoConfig.initIdentityScope(type);

        goldDaoConfig = daoConfigMap.get(GoldDao.class).clone();
        goldDaoConfig.initIdentityScope(type);

        iMessageDaoConfig = daoConfigMap.get(IMessageDao.class).clone();
        iMessageDaoConfig.initIdentityScope(type);

        nameListDaoConfig = daoConfigMap.get(NameListDao.class).clone();
        nameListDaoConfig.initIdentityScope(type);

        contactDaoConfig = daoConfigMap.get(ContactDao.class).clone();
        contactDaoConfig.initIdentityScope(type);

        expressionGroupDaoConfig = daoConfigMap.get(ExpressionGroupDao.class).clone();
        expressionGroupDaoConfig.initIdentityScope(type);

        expressionDaoConfig = daoConfigMap.get(ExpressionDao.class).clone();
        expressionDaoConfig.initIdentityScope(type);

        conversationDao = new ConversationDao(conversationDaoConfig, this);
        dynamicDao = new DynamicDao(dynamicDaoConfig, this);
        goldDao = new GoldDao(goldDaoConfig, this);
        iMessageDao = new IMessageDao(iMessageDaoConfig, this);
        nameListDao = new NameListDao(nameListDaoConfig, this);
        contactDao = new ContactDao(contactDaoConfig, this);
        expressionGroupDao = new ExpressionGroupDao(expressionGroupDaoConfig, this);
        expressionDao = new ExpressionDao(expressionDaoConfig, this);

        registerDao(Conversation.class, conversationDao);
        registerDao(Dynamic.class, dynamicDao);
        registerDao(Gold.class, goldDao);
        registerDao(IMessage.class, iMessageDao);
        registerDao(NameList.class, nameListDao);
        registerDao(Contact.class, contactDao);
        registerDao(ExpressionGroup.class, expressionGroupDao);
        registerDao(Expression.class, expressionDao);
    }
    
    public void clear() {
        conversationDaoConfig.clearIdentityScope();
        dynamicDaoConfig.clearIdentityScope();
        goldDaoConfig.clearIdentityScope();
        iMessageDaoConfig.clearIdentityScope();
        nameListDaoConfig.clearIdentityScope();
        contactDaoConfig.clearIdentityScope();
        expressionGroupDaoConfig.clearIdentityScope();
        expressionDaoConfig.clearIdentityScope();
    }

    public ConversationDao getConversationDao() {
        return conversationDao;
    }

    public DynamicDao getDynamicDao() {
        return dynamicDao;
    }

    public GoldDao getGoldDao() {
        return goldDao;
    }

    public IMessageDao getIMessageDao() {
        return iMessageDao;
    }

    public NameListDao getNameListDao() {
        return nameListDao;
    }

    public ContactDao getContactDao() {
        return contactDao;
    }

    public ExpressionGroupDao getExpressionGroupDao() {
        return expressionGroupDao;
    }

    public ExpressionDao getExpressionDao() {
        return expressionDao;
    }

}
