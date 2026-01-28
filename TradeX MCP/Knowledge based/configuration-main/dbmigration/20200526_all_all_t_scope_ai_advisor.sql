INSERT INTO t_scope(id, name, uri_pattern, forward_type, forward_data)VALUES(553, "AI_ADVISOR_DIARY_LIST", "get:/api/v2/aiAdvisor/diary", 'CONNECTION', '{"service":"ai-advisor","uri":"/api/v2/aiAdvisor/diary","forwardType":"SERVICE"}');
INSERT INTO t_scope(id, name, uri_pattern, forward_type, forward_data)VALUES(554, "AI_ADVISOR_NEWS_LIST", "get:/api/v2/aiAdvisor/news", 'CONNECTION', '{"service":"ai-advisor","uri":"/api/v2/aiAdvisor/news","forwardType":"SERVICE"}');
INSERT INTO t_scope(id, name, uri_pattern, forward_type, forward_data)VALUES(555, "AI_ADVISOR_DIARY", "get:/api/v2/aiAdvisor/diary/{id}", 'CONNECTION', '{"service":"ai-advisor","uri":"/api/v2/aiAdvisor/diary/{id}","forwardType":"SERVICE"}');
INSERT INTO t_scope(id, name, uri_pattern, forward_type, forward_data)VALUES(556, "AI_ADVISOR_NEWS", "get:/api/v2/aiAdvisor/news/{id}", 'CONNECTION', '{"service":"ai-advisor","uri":"/api/v2/aiAdvisor/news/{id}","forwardType":"SERVICE"}');



INSERT INTO `tradex-configuration`.t_scope_scope_group_map (scope_id,scope_group_id) select s.id, sg.id from t_scope s inner join t_scope_group sg on s.name = 'AI_ADVISOR_DIARY_LIST' and sg.scope_group_name = 'PUBLIC';
INSERT INTO `tradex-configuration`.t_scope_scope_group_map (scope_id,scope_group_id) select s.id, sg.id from t_scope s inner join t_scope_group sg on s.name = 'AI_ADVISOR_NEWS_LIST' and sg.scope_group_name = 'PUBLIC';
INSERT INTO `tradex-configuration`.t_scope_scope_group_map (scope_id,scope_group_id) select s.id, sg.id from t_scope s inner join t_scope_group sg on s.name = 'AI_ADVISOR_DIARY' and sg.scope_group_name = 'PUBLIC';
INSERT INTO `tradex-configuration`.t_scope_scope_group_map (scope_id,scope_group_id) select s.id, sg.id from t_scope s inner join t_scope_group sg on s.name = 'AI_ADVISOR_NEWS' and sg.scope_group_name = 'PUBLIC';